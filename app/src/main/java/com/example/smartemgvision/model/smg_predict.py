from flask import Flask, request, jsonify
import numpy as np
import pandas as pd
from tensorflow.keras.models import load_model
from sklearn.preprocessing import StandardScaler
import os

# Configuración inicial
app = Flask(__name__)

# Cargar modelo y datos
model = load_model("../model.keras")
data = pd.read_csv("./pe.csv", header=None)

# Mapeo de palabras clave a etiquetas
label_map = {"tip": 1, "spherical": 4, "lateral": 5}

# Función para obtener una fila aleatoria con la etiqueta deseada
def get_random_row(data, label):
    filtered_data = data[data.iloc[:, -1] == label]
    if filtered_data.empty:
        return None  
    return filtered_data.sample(n=1).iloc[0].values 

# Endpoint para realizar predicción
@app.route('/predict', methods=['POST'])
def predict():
    # Obtener el dato enviado al servidor
    input_data = request.json
    keyword = input_data.get("keyword", "").lower()

    # Validar si la palabra clave es reconocida
    if keyword not in label_map:
        return jsonify({"error": f"Palabra clave '{keyword}' no reconocida."}), 400

    # Obtener etiqueta correspondiente
    label = label_map[keyword]
    random_row = get_random_row(data, label)
    if random_row is None:
        return jsonify({"error": f"No se encontraron filas con la etiqueta '{label}'."}), 404

    # Separar características (X) y etiqueta real
    X_new = random_row[:-1]
    y_actual = random_row[-1]

    # Normalizar los datos
    std = StandardScaler()
    std.fit(data.iloc[:, :-1].values)  
    X_new_normalized = std.transform(X_new.reshape(1, -1))

    # Hacer predicción
    prediction = model.predict(X_new_normalized)
    predicted_class = int(np.argmax(prediction))  # Convertir a entero para JSON

    # Responder con la clase predicha
    return jsonify({
        "predicted_class": predicted_class,
        "probabilities": prediction.tolist(),  # Convertir a lista para JSON
        "actual_label": int(y_actual)
    })

def clear_console():
    os.system('cls' if os.name == 'nt' else 'clear')


# Ejecutar servidor
if __name__ == "__main__":

    clear_console()

    app.run(host='0.0.0.0', port=5001, debug=True)
