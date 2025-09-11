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
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

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

# Enhanced retriever configuration for better coverage
retriever = docsearch.as_retriever(
    search_type="similarity_score_threshold",  # Use score threshold for better quality
    search_kwargs={
        "k": 15,  # Retrieve more documents
        "score_threshold": 0.1,  # Lower threshold to be more inclusive
    }
)

# Backup retriever for broader search
backup_retriever = docsearch.as_retriever(
    search_type="mmr",
    search_kwargs={
        "k": 20,
        "fetch_k": 30,
        "lambda_mult": 0.5  # More diversity
    }
)

# Initialize LLM
llm = ChatGroq(
    groq_api_key=GROQ_API_KEY,
    model_name="qwen/qwen3-32b",
    temperature=0.3,  # Lower temperature for more focused responses
    max_tokens=4096,
)

# Enhanced system prompt with specific instructions for Creator Boost data
system_prompt = """You are a specialized Creator Boost platform assistant. Your knowledge comes from the platform's official documentation and features.

CORE INSTRUCTIONS:
1. ALWAYS prioritize information from the provided context over general knowledge
2. Be specific and detailed when context contains relevant information
3. For Creator Boost topics with insufficient context, acknowledge the limitation but provide what you can
4. For unrelated topics, politely redirect to Creator Boost queries

PLATFORM KNOWLEDGE AREAS:
- User authentication (registration, login, verification, password reset)
- Profile management (editing, uploading images, skills, certifications)
- Chat system (messaging, conversations, real-time communication)
- Services (browsing, packages, reviews, expert offerings)
- Supported platforms (YouTube, Instagram, TikTok, Facebook, LinkedIn, Twitter)
- Platform statistics (50K+ users, 10K+ services, 2K+ experts, 4.9/5 rating)

RESPONSE STYLE:
- Be conversational and helpful
- Use specific details from context when available
- Provide actionable guidance
- Mention relevant features and capabilities

CONTEXT: {context}

Remember: You are the official Creator Boost assistant. Provide accurate, helpful information based on the platform's actual features and data."""

prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    ("human", "{input}"),
])

question_answer_chain = create_stuff_documents_chain(llm, prompt)
rag_chain = create_retrieval_chain(retriever, question_answer_chain)

def enhanced_query_preprocessing(query):
    """Enhanced query preprocessing with better synonym mapping"""
    query = query.strip().lower()
    
    # Enhanced synonym mapping based on your PDF data
    synonyms = {
        # Authentication related
        'sign up': 'registration signup register create account',
        'sign in': 'login authentication sign in',
        'log in': 'login authentication sign in',
        'verification': 'email verification OTP verify account activation',
        'password reset': 'forgot password reset password OTP',
        
        # Platform features
        'messaging': 'chat messages conversation real-time messaging',
        'profile': 'profile management edit update skills certifications',
        'services': 'services packages experts freelancers offerings',
        'experts': 'experts freelancers providers skilled professionals',
        'clients': 'clients customers users hiring',
        
        # Platforms
        'youtube': 'YouTube video content growth strategy',
        'instagram': 'Instagram content hashtags social media',
        'tiktok': 'TikTok viral content creation',
        'facebook': 'Facebook marketing ads social media',
        'linkedin': 'LinkedIn business networking B2B',
        'twitter': 'Twitter engagement growth strategy',
        
        # Common abbreviations
        'auth': 'authentication login security access',
        'api': 'API endpoints services integration',
        'ui': 'user interface design experience',
        'otp': 'OTP one time password verification'
    }
    
    # Apply synonyms
    for key, expansion in synonyms.items():
        if key in query:
            query = f"{query} {expansion}"
    
    # Add context for specific topics
    if any(word in query for word in ['how', 'what', 'where', 'when', 'why']):
        if 'profile' in query:
            query += ' profile management editing uploading'
        elif any(platform in query for platform in ['youtube', 'instagram', 'tiktok', 'facebook', 'linkedin', 'twitter']):
            query += ' platform services experts growth strategy'
        elif any(auth_term in query for auth_term in ['login', 'register', 'signup', 'verification']):
            query += ' authentication user account management'
    
    logger.info(f"Enhanced query: {query}")
    return query

def get_creator_boost_context_score(docs, query):
    """Score how well the retrieved documents match Creator Boost content"""
    if not docs:
        return 0
    
    creator_boost_terms = [
        'creator boost', 'platform', 'expert', 'client', 'service', 'profile',
        'authentication', 'login', 'register', 'verification', 'chat', 'message',
        'youtube', 'instagram', 'tiktok', 'facebook', 'linkedin', 'twitter',
        'rating', 'review', 'package', 'delivery', 'portfolio', 'skill'
    ]
    
    total_score = 0
    query_lower = query.lower()
    
    for doc in docs:
        content_lower = doc.page_content.lower()
        doc_score = 0
        
        # Check for Creator Boost specific terms
        for term in creator_boost_terms:
            if term in content_lower:
                doc_score += 1
        
        # Bonus for query term matches
        for word in query_lower.split():
            if len(word) > 3 and word in content_lower:
                doc_score += 2
        
        total_score += doc_score
    
    # Normalize score
    max_possible_score = len(docs) * (len(creator_boost_terms) + 10)
    normalized_score = min(total_score / max_possible_score, 1.0) if max_possible_score > 0 else 0
    
    logger.info(f"Context relevance score: {normalized_score:.2f}")
    return normalized_score

def enhanced_response_cleaning(response_text, query):
    """Clean and enhance response based on query context"""
    if not response_text:
        return None
    
    cleaned_text = response_text.strip()
    
    # Remove redundant phrases more aggressively
    redundant_patterns = [
        r"based on (the )?(provided )?(context|information|document)",
        r"according to (the )?(context|information|document)",
        r"(the )?(context|document|information) (states|mentions|indicates|shows|suggests)",
        r"from (the )?(provided )?(context|information)",
        r"as (mentioned|stated|indicated) in (the )?(context|document)",
        r"(unfortunately|sorry),?\s*i (don't|do not) have",
        r"(please )?note that",
        r"it (appears|seems) that",
        r"(the|this) (question|query) (is about|relates to)"
    ]
    
    for pattern in redundant_patterns:
        cleaned_text = re.sub(pattern, "", cleaned_text, flags=re.IGNORECASE)
    
    # Clean whitespace and formatting
    cleaned_text = re.sub(r'\s+', ' ', cleaned_text).strip()
    cleaned_text = re.sub(r'^[,.;:\-\s]+', '', cleaned_text)
    
    # Ensure proper capitalization
    if cleaned_text and cleaned_text[0].islower():
        cleaned_text = cleaned_text[0].upper() + cleaned_text[1:]
    
    # Filter out very short or generic responses
    if len(cleaned_text.split()) < 5:
        return None
    
    # Filter out generic non-answers
    generic_phrases = [
        "i don't have information",
        "i cannot provide",
        "i'm not sure",
        "i don't know",
        "no information available"
    ]
    
    if any(phrase in cleaned_text.lower() for phrase in generic_phrases) and len(cleaned_text) < 100:
        return None
    
    return cleaned_text

def is_creator_boost_query(message):
    """Enhanced detection for Creator Boost related queries"""
    creator_boost_indicators = {
        # Platform core terms
        'platform_terms': ['creator', 'boost', 'creatorboost', 'platform', 'expert', 'freelancer', 'service'],
        
        # User journey terms  
        'user_terms': ['client', 'user', 'profile', 'account', 'dashboard'],
        
        # Authentication terms
        'auth_terms': ['login', 'register', 'signup', 'verification', 'password', 'otp', 'authentication'],
        
        # Feature terms
        'feature_terms': ['chat', 'message', 'conversation', 'portfolio', 'review', 'rating', 'package'],
        
        # Platform names
        'platform_names': ['youtube', 'instagram', 'tiktok', 'facebook', 'linkedin', 'twitter'],
        
        # Technical terms that might relate to the platform
        'tech_terms': ['api', 'system', 'database', 'upload', 'edit', 'update', 'search', 'browse']
    }
    
    message_lower = message.lower()
    
    # Check each category
    for category, terms in creator_boost_indicators.items():
        if any(term in message_lower for term in terms):
            logger.info(f"Query matched category: {category}")
            return True
    
    # Check for question patterns that might be platform-related
    question_patterns = [
        r"how (do i|can i|to).+",
        r"what (is|are|does|can).+",
        r"where (do i|can i|to).+",
        r"why (do i|should i|can't i).+",
        r"(can i|may i|is it possible).+"
    ]
    
    for pattern in question_patterns:
        if re.match(pattern, message_lower):
            # If it's a how-to question, it's likely platform related
            return True
    
    return False

def get_fallback_response(query):
    """Generate appropriate fallback responses based on query type"""
    query_lower = query.lower()
    
    # Authentication related fallbacks
    if any(term in query_lower for term in ['login', 'register', 'signup', 'password', 'verification', 'otp']):
        return "Creator Boost offers secure authentication with email verification. You can register as either a client or expert, verify your email with OTP, reset passwords, and manage your account securely. Would you like specific details about any authentication feature?"
    
    # Profile related fallbacks
    if any(term in query_lower for term in ['profile', 'edit', 'update', 'upload', 'image', 'skill', 'certification']):
        return "Creator Boost profiles are fully customizable. Experts can showcase skills, certifications, languages, and portfolios, while clients can set preferences. You can upload profile images and edit your information anytime. What specific profile feature interests you?"
    
    # Platform/services related fallbacks
    if any(platform in query_lower for platform in ['youtube', 'instagram', 'tiktok', 'facebook', 'linkedin', 'twitter']):
        return "Creator Boost supports all major social media platforms including YouTube, Instagram, TikTok, Facebook, LinkedIn, and Twitter. Our experts offer services like growth strategies, content creation, and audience engagement. Which platform are you looking to grow?"
    
    # Chat/messaging fallbacks
    if any(term in query_lower for term in ['chat', 'message', 'conversation', 'contact']):
        return "Creator Boost features real-time messaging between clients and experts. You can start conversations from profiles, view chat history, and receive instant notifications. The system supports file attachments and maintains conversation histories."
    
    # General platform fallback
    return "Creator Boost is a platform connecting clients with skilled experts across various fields. With 50K+ users, 2K+ experts, and a 4.9/5 rating, we offer secure payments, verified experts, and quality guarantees. How can I help you with Creator Boost today?"

@app.route("/")
def home():
    return jsonify({
        "message": "Creator Boost Chatbot API is running!",
        "status": "active",
        "version": "3.0 - Enhanced PDF Response System"
    })

@app.route("/api/chat", methods=["POST"])
def chat():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"answer": "No data provided"}), 400
            
        msg = data.get("msg", "").strip()
        
        if not msg:
            return jsonify({"answer": "Please provide a message"}), 400
        
        logger.info(f"Received query: {msg}")
        
        msg_lower = msg.lower()
        
        # Enhanced greeting patterns
        greeting_patterns = [
            r"^(hi|hello|hey|greetings|good (morning|afternoon|evening))",
            r"^how are you",
            r"^what can you do",
            r"^who are you",
            r"^help$",
            r"^start$"
        ]
        
        for pattern in greeting_patterns:
            if re.match(pattern, msg_lower):
                return jsonify({
                    "answer": "Hello! I'm your Creator Boost assistant. I can help you with platform features, user accounts, messaging, services, social media growth strategies, and more. Creator Boost connects 50K+ users with 2K+ expert freelancers across YouTube, Instagram, TikTok, and other platforms. What would you like to know?"
                })
        
        # Check if question is related to Creator Boost
        if not is_creator_boost_query(msg):
            return jsonify({"answer": "I can only help with Creator Boost related queries. Try asking about user accounts, services, experts, social media growth, or platform features!"})
        
        # Enhanced query preprocessing
        enhanced_query = enhanced_query_preprocessing(msg)
        
        # Primary retrieval attempt
        try:
            response = rag_chain.invoke({"input": enhanced_query})
            raw_answer = response.get("answer", "").strip()
            context_docs = response.get("context", [])
            
            logger.info(f"Primary retrieval: {len(context_docs)} docs")
            
            # Check context quality
            context_score = get_creator_boost_context_score(context_docs, enhanced_query)
            
            # If primary retrieval doesn't yield good results, try backup
            if context_score < 0.3 or len(context_docs) < 3:
                logger.info("Trying backup retrieval strategy")
                backup_docs = backup_retriever.invoke(enhanced_query)
                if backup_docs:
                    # Create a new response with backup docs
                    backup_context = "\n\n".join([doc.page_content for doc in backup_docs[:10]])
                    backup_prompt = f"""Context: {backup_context}\n\nQuestion: {msg}\n\nProvide a helpful answer about Creator Boost based on the context."""
                    backup_response = llm.invoke(backup_prompt)
                    raw_answer = backup_response.content if hasattr(backup_response, 'content') else str(backup_response)
                    context_docs = backup_docs
                    context_score = get_creator_boost_context_score(backup_docs, enhanced_query)
                    logger.info(f"Backup retrieval: {len(backup_docs)} docs, score: {context_score:.2f}")
            
            # Clean and validate response
            cleaned_answer = enhanced_response_cleaning(raw_answer, msg)
            
            # Enhanced fallback logic
            if not cleaned_answer or len(cleaned_answer.split()) < 8:
                logger.info("Using intelligent fallback")
                fallback_answer = get_fallback_response(msg)
                return jsonify({"answer": fallback_answer})
            
            # Additional validation - ensure response is Creator Boost focused
            if context_score > 0.2:  # We have some relevant context
                return jsonify({"answer": cleaned_answer})
            else:
                # Context score is low, use fallback
                fallback_answer = get_fallback_response(msg)
                return jsonify({"answer": fallback_answer})
                
        except Exception as retrieval_error:
            logger.error(f"Retrieval error: {str(retrieval_error)}")
            fallback_answer = get_fallback_response(msg)
            return jsonify({"answer": fallback_answer})
        
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}")
        return jsonify({
            "answer": "I'm experiencing technical difficulties. Please try rephrasing your question about Creator Boost features, and I'll do my best to help."
        }), 500

@app.route("/api/debug", methods=["POST"])
def debug_retrieval():
    """Enhanced debug endpoint"""
    try:
        data = request.get_json()
        msg = data.get("msg", "").strip()
        
        if not msg:
            return jsonify({"error": "No message provided"}), 400
        
        # Test both retrievers
        enhanced_query = enhanced_query_preprocessing(msg)
        primary_docs = retriever.invoke(enhanced_query)
        backup_docs = backup_retriever.invoke(enhanced_query)
        
        debug_info = {
            "original_query": msg,
            "enhanced_query": enhanced_query,
            "is_creator_boost_related": is_creator_boost_query(msg),
            "primary_retrieval": {
                "num_docs": len(primary_docs),
                "context_score": get_creator_boost_context_score(primary_docs, enhanced_query),
                "docs_preview": [
                    {
                        "content_preview": doc.page_content[:200] + "..." if len(doc.page_content) > 200 else doc.page_content,
                        "metadata": doc.metadata,
                        "length": len(doc.page_content)
                    }
                    for doc in primary_docs[:3]
                ]
            },
            "backup_retrieval": {
                "num_docs": len(backup_docs),
                "context_score": get_creator_boost_context_score(backup_docs, enhanced_query),
                "docs_preview": [
                    {
                        "content_preview": doc.page_content[:200] + "..." if len(doc.page_content) > 200 else doc.page_content,
                        "metadata": doc.metadata,
                        "length": len(doc.page_content)
                    }
                    for doc in backup_docs[:3]
                ]
            }
        }
        
        return jsonify(debug_info)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8088, debug=False)