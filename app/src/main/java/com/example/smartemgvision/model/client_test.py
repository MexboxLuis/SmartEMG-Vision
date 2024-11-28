import requests
import cv2
import numpy as np

url = "http://127.0.0.1:5000/detect-objects"


image_path = "your_path_image_test.jpg"

original_image = cv2.imread(image_path)

if original_image is None:
    print("Error al cargar la imagen desde el cliente.")
    exit()


with open(image_path, "rb") as image_file:
    files = {'file': image_file}
    response = requests.post(url, files=files)

if response.status_code == 200:
    detections = response.json().get("detections", [])
    print("Detecciones:")

    for detection in detections:
        label = detection['label']
        confidence = detection['confidence']
        box = detection['box']

        print(f"Etiqueta: {label}, Confianza: {confidence:.2f}, Cuadro: {box}")

        x1, y1, x2, y2 = map(int, box)
        cv2.rectangle(original_image, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(
            original_image,
            f"{label} ({confidence:.2f})",
            (x1, y1 - 10),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.5,
            (0, 255, 0),
            2
        )

    cv2.imshow("Detecciones", original_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

else:
    print(f"Error: {response.status_code}, {response.text}")
