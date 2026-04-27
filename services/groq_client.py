import os
import time
import json
from groq import Groq
from dotenv import load_dotenv

# ✅ Load .env correctly
load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), '..', '.env'))

# ✅ Get API key
api_key = os.getenv("GROQ_API_KEY")

if not api_key:
    raise ValueError("❌ GROQ_API_KEY not found in .env")

print("✅ GROQ API KEY LOADED")

# ✅ Create client
client = Groq(api_key=api_key)


def call_groq(prompt):
    for attempt in range(3):
        try:
            response = client.chat.completions.create(
                # ✅ UPDATED WORKING MODEL
                model="llama-3.3-70b-versatile",
                messages=[
                    {"role": "user", "content": prompt}
                ],
                temperature=0.3,
                max_tokens=500
            )

            content = response.choices[0].message.content

            if not content:
                raise ValueError("Empty AI response")

            return content

        except Exception as e:
            print(f"❌ ERROR (Attempt {attempt+1}):", e)
            time.sleep(2)

    return json.dumps({
        "category": "Unknown",
        "confidence": 0.0,
        "reasoning": "AI service unavailable",
        "is_fallback": True
    })