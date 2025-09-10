from flask import Flask, jsonify, request
from flask_cors import CORS
from src.helper import download_hugging_face_embeddings
from langchain_pinecone import PineconeVectorStore
from langchain_groq import ChatGroq
from langchain.chains import create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain_core.prompts import ChatPromptTemplate
from dotenv import load_dotenv
import os
import re

app = Flask(__name__)
CORS(app)

load_dotenv()

PINECONE_API_KEY = os.environ.get('PINECONE_API_KEY')
GROQ_API_KEY = os.environ.get('GROQ_API_KEY')

# Initialize embeddings and vector store
embeddings = download_hugging_face_embeddings()
index_name = "creatorboost"

docsearch = PineconeVectorStore.from_existing_index(
    index_name=index_name,
    embedding=embeddings
)

retriever = docsearch.as_retriever(search_type="similarity", search_kwargs={"k": 9})  # Increased k for better context

# Initialize LLM
llm = ChatGroq(
    groq_api_key=GROQ_API_KEY,
    model_name="qwen/qwen3-32b",
    temperature=0.4,  # Balanced temperature
    max_tokens=800,   # Increased for more detailed but concise responses
)

# Enhanced system prompt for better responses
system_prompt = """You are a knowledgeable assistant for the Creator Boost platform. 
STRICT RULES:
1. Answer ONLY based on the provided context about Creator Boost
2. If question is completely unrelated to Creator Boost, respond with "I don't know"
3. If answer is not in context but question is related, say "I don't have that specific information"

5. Be factual and helpful
6. No apologies or meta-commentary
7. For team member queries, provide their contributions if available

Context: {context}"""

prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    ("human", "{input}"),
])

question_answer_chain = create_stuff_documents_chain(llm, prompt)
rag_chain = create_retrieval_chain(retriever, question_answer_chain)

def clean_response(response_text):
    """Clean and format the response to remove unnecessary parts"""
    if not response_text:
        return "I don't know."
    
    # Remove common verbose patterns
    patterns_to_remove = [
        r"based on (the |this )?(provided )?(context|information|document)",
        r"according to (the |this )?(provided )?(context|information|document)",
        r"the (provided )?(context|document|information) (states|indicates|shows|says)",
        r"let me (help|assist|explain)",
        r"here'?s? (what I found|the information|the answer)",
        r"to answer your question",
        r"in (summary|conclusion)",
        r"however|additionally|furthermore|therefore",
        r"i (think|believe|would say)",
        r"please note that",
        r"kindly|actually|basically|essentially",
        r"unfortunately|sorry|apologize",
        r"as (mentioned|stated|discussed) (above|previously|earlier)"
    ]
    
    cleaned_text = response_text.strip()
    
    for pattern in patterns_to_remove:
        cleaned_text = re.sub(pattern, "", cleaned_text, flags=re.IGNORECASE)
    
    # Clean up punctuation and whitespace
    cleaned_text = re.sub(r'\s+', ' ', cleaned_text).strip()
    cleaned_text = re.sub(r'[.,;:]+$', '', cleaned_text)
    
    # Ensure proper capitalization
    if cleaned_text and cleaned_text[0].islower():
        cleaned_text = cleaned_text[0].upper() + cleaned_text[1:]
    
    # Limit to 4 sentences maximum but allow more detail for relevant responses
    sentences = re.split(r'(?<=[.!?])\s+', cleaned_text)
    if len(sentences) > 4:
        cleaned_text = ' '.join(sentences[:4])
    
    # Final cleanup
    cleaned_text = cleaned_text.strip()
    
    if not cleaned_text or len(cleaned_text) < 10:  # More lenient minimum length
        return "I don't know."
    
    return cleaned_text

def is_creator_boost_related(message):
    """Check if the message is related to Creator Boost"""
    creator_boost_keywords = [
        'creator', 'boost', 'platform', 'project', 'software', 
        'architecture', 'requirements', 'specification', 'api',
        'user', 'client', 'freelancer', 'gig', 'payment', 'dashboard',
        'jwt', 'otp', 'kafka', 'stripe', 'database', 'system'
    ]
    
    message_lower = message.lower()
    return any(keyword in message_lower for keyword in creator_boost_keywords)

@app.route("/")
def home():
    return jsonify({"message": "Creator Boost Chatbot API is running!", "status": "active"})

@app.route("/api/chat", methods=["POST"])
def chat():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"answer": "No data provided"}), 400
            
        msg = data.get("msg", "").strip()
        
        if not msg:
            return jsonify({"answer": "Please provide a message"}), 400
        
        msg_lower = msg.lower()
        
        # Handle greetings
        greeting_patterns = [
            r"^(hi|hello|hey|greetings|good (morning|afternoon|evening))",
            r"^how are you",
            r"^what can you do",
            r"^who are you"
        ]
        
        for pattern in greeting_patterns:
            if re.match(pattern, msg_lower):
                return jsonify({"answer": "Hello! I'm the Creator Boost assistant. I can help you with information about the platform's features, architecture, team members, and technical details. What would you like to know?"})
        
        # Check if question is completely unrelated
        if not is_creator_boost_related(msg):
            return jsonify({"answer": "I don't know."})
        
        # Get response from RAG chain
        response = rag_chain.invoke({"input": msg})
        raw_answer = response.get("answer", "").strip()
        
        # Clean and format the response
        answer = clean_response(raw_answer)
        
        # Enhanced fallback logic
        if (not answer or 
            answer.lower() in ["i don't know", "i don't have that information", ""] or 
            (len(answer.split()) < 4 and "don't know" not in answer.lower())):
            
            if is_creator_boost_related(msg):
                return jsonify({"answer": "I don't have that specific information about Creator Boost in my knowledge base."})
            else:
                return jsonify({"answer": "I don't know."})
        
        return jsonify({"answer": answer})
        
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"answer": "Sorry, I encountered an error processing your request."}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8080, debug=False)