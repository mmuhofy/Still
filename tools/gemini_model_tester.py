import requests
import json
import time

API_BASE = "https://generativelanguage.googleapis.com/v1beta"

api_key = input("Gemini API Key: ").strip()


def get_models():
    url = f"{API_BASE}/models?key={api_key}"

    r = requests.get(url)

    if r.status_code != 200:
        print("Model listesi alınamadı!")
        print(r.text)
        exit()

    return r.json().get("models", [])


def test_model(model_name):
    clean_name = model_name.replace("models/", "")

    url = f"{API_BASE}/models/{clean_name}:generateContent?key={api_key}"

    payload = {
        "contents": [
            {
                "parts": [
                    {
                        "text": "Sadece selam yaz."
                    }
                ]
            }
        ]
    }

    try:
        r = requests.post(
            url,
            headers={
                "Content-Type": "application/json"
            },
            data=json.dumps(payload),
            timeout=20
        )

        if r.status_code != 200:
            return False, f"HTTP {r.status_code}"

        data = r.json()

        text = (
            data["candidates"][0]["content"]["parts"][0]["text"]
        )

        return True, text.strip()

    except Exception as e:
        return False, str(e)


print("\nModeller alınıyor...\n")

models = get_models()

# Sadece text modelleri
filtered_models = []

for model in models:

    methods = model.get("supportedGenerationMethods", [])
    name = model.get("name", "").lower()

    # image / embedding / vision modellerini ele
    blocked = [
        "embedding",
        "vision",
        "image",
        "aqa"
    ]

    if "generateContent" not in methods:
        continue

    if any(x in name for x in blocked):
        continue

    filtered_models.append(model["name"])

print(f"Bulunan text modeli: {len(filtered_models)}\n")

for model in filtered_models:

    print(f"[TEST] {model}")

    ok, result = test_model(model)

    if ok:
        print(f"[OK] {model}")
        print("Cevap:", result)

    else:
        print(f"[FAIL] {model}")
        print(result)

    print("-" * 50)

    time.sleep(1)
