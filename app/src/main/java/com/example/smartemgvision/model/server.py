from flask import Flask, request, jsonify
from ultralytics import YOLO
import numpy as np
import cv2
import os
import signal
import shutil

app = Flask(__name__)
model = YOLO('yolov8n.pt')

image_counter = 1
image_dir = "received_images"
allowed_labels = [
    "person", "spoon", "knife", "fork", "bottle", "cup", "bowl",
    "apple", "banana", "sandwich", "broccoli", "orange", "carrot", "hot dog",
    "pizza", "donut", "cake", "bench", "chair", "couch", "bed", "dining table"
]


def clear_console():
    os.system('cls' if os.name == 'nt' else 'clear')

# Endpoint para detectar objetos
@app.route('/detect-objects', methods=['POST'])
def detect_objects():
    global image_counter
    try:
        if 'file' not in request.files:
            return jsonify({"error": "No se encontró el archivo en la solicitud."}), 400

        file = request.files['file']
        if file.filename == '':
            return jsonify({"error": "Nombre de archivo vacío."}), 400

        file_bytes = file.read()
        print(f"Received data length: {len(file_bytes)}")

        # Asegurarse de que el directorio existe
        os.makedirs(image_dir, exist_ok=True)

        # Guardar la imagen con un nombre en orden
        filename = f"{image_counter}.jpg"
        filepath = os.path.join(image_dir, filename)
        image_counter += 1

        with open(filepath, 'wb') as f:
            f.write(file_bytes)
        print(f"Imagen guardada como {filepath}")

        # Leer la imagen y procesarla
        nparr = np.frombuffer(file_bytes, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if frame is None or frame.size == 0:
            return jsonify({"error": "Frame inválido o vacío."}), 400

        results = model(frame)

        detections = []
        height, width, _ = frame.shape  # Dimensiones de la imagen original
        for result in results:
            for box in result.boxes:
                label = result.names[int(box.cls)]  # Obtén el nombre de la etiqueta
                if label not in allowed_labels:     # Filtra las etiquetas irrelevantes
                    continue

                x_min, y_min, x_max, y_max = box.xyxy[0].tolist()
                detections.append({
                    "label": label,
                    "confidence": float(box.conf),
                    "box": [
                        x_min / width,  # Normalizar respecto al ancho
                        y_min / height, # Normalizar respecto a la altura
                        x_max / width,
                        y_max / height
                    ]
                })

        return jsonify({"detections": detections}), 200

    except Exception as e:
        print("Error:", str(e))
        return jsonify({"error": "Error procesando el frame."}), 500

# Limpiar carpeta de imágenes al cerrar el servidor
def cleanup():
    if os.path.exists(image_dir):
        shutil.rmtree(image_dir)
        print(f"Carpeta {image_dir} eliminada.")

# Señal para capturar el cierre del servidor
def handle_exit(_signal, _frame):
    print("\nCerrando servidor...")
    cleanup()
    exit(0)


if __name__ == '__main__':
    # Limpiar consola
    clear_console()

    # Registrar la señal para capturar interrupciones (Ctrl+C)
    signal.signal(signal.SIGINT, handle_exit)
    signal.signal(signal.SIGTERM, handle_exit)

    print("Servidor iniciado. Presiona Ctrl+C para cerrar.")
    app.run(host='0.0.0.0', port=5000, debug=True)
