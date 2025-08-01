<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Course Recommendations - Learning Platform</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/homePage.css">
    <style>
        .ai-badge {
            background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
            color: white;
            font-size: 0.75rem;
            padding: 0.25rem 0.5rem;
            border-radius: 1rem;
            position: absolute;
            top: 10px;
            right: 10px;
            z-index: 10;
        }
        
        .course-card {
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .course-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
        }
        
        .ai-powered-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem 0;
            margin-bottom: 2rem;
        }
        
        .preference-selector {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .preference-btn {
            margin: 0.25rem;
            border-radius: 20px;
        }
        
        .chatbot-integration {
            position: fixed;
            bottom: 20px;
            right: 20px;
            z-index: 1000;
        }
        
        .loading-spinner {
            display: none;
        }
    </style>
</head>
<body>
    <jsp:include page="navbar.jsp" />
    
    <!-- AI-Powered Header -->
    <div class="ai-powered-header">
        <div class="container">
            <div class="text-center">
                <h1 class="display-4 fw-bold mb-3">
                    <i class="bi bi-robot me-3"></i>
                    AI-Powered Course Recommendations
                </h1>
                <p class="lead mb-0">
                    Personalized learning paths generated by artificial intelligence
                </p>
                <c:if test="${aiGenerated}">
                    <span class="badge bg-success mt-2">
                        <i class="bi bi-check-circle me-1"></i>
                        Generated by AI
                    </span>
                </c:if>
                <c:if test="${not aiGenerated}">
                    <span class="badge bg-warning mt-2">
                        <i class="bi bi-exclamation-triangle me-1"></i>
                        Fallback Recommendations
                    </span>
                </c:if>
            </div>
        </div>
    </div>
    
    <div class="container">
        <!-- Error Message -->
        <c:if test="${not empty error}">
            <div class="alert alert-warning alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <!-- Preference Selector -->
        <div class="preference-selector">
            <h5 class="mb-3">
                <i class="bi bi-sliders me-2"></i>
                Refine Your Recommendations
            </h5>
            <p class="text-muted mb-3">Tell us what you're interested in learning:</p>
            
            <div class="d-flex flex-wrap">
                <button class="btn btn-outline-primary preference-btn" data-preference="beginner">
                    <i class="bi bi-star me-1"></i>
                    Beginner Friendly
                </button>
                <button class="btn btn-outline-primary preference-btn" data-preference="web-development">
                    <i class="bi bi-globe me-1"></i>
                    Web Development
                </button>
                <button class="btn btn-outline-primary preference-btn" data-preference="mobile-apps">
                    <i class="bi bi-phone me-1"></i>
                    Mobile Apps
                </button>
                <button class="btn btn-outline-primary preference-btn" data-preference="data-science">
                    <i class="bi bi-graph-up me-1"></i>
                    Data Science
                </button>
                <button class="btn btn-outline-primary preference-btn" data-preference="game-development">
                    <i class="bi bi-controller me-1"></i>
                    Game Development
                </button>
                <button class="btn btn-outline-primary preference-btn" data-preference="system-programming">
                    <i class="bi bi-cpu me-1"></i>
                    System Programming
                </button>
            </div>
            
            <div class="mt-3">
                <button id="refreshRecommendations" class="btn btn-primary">
                    <i class="bi bi-arrow-clockwise me-2"></i>
                    Update Recommendations
                </button>
                <div class="loading-spinner d-inline-block ms-2">
                    <div class="spinner-border spinner-border-sm" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Recommended Courses -->
        <div id="recommendationsContainer">
            <h3 class="mb-4">
                <i class="bi bi-lightbulb-fill me-2"></i>
                Recommended for You
            </h3>
            
            <div class="row" id="courseCards">
                <c:forEach var="course" items="${recommendedCourses}" varStatus="status">
                    <div class="col-lg-4 col-md-6 mb-4">
                        <div class="card course-card h-100 border-0 shadow">
                            <c:if test="${aiGenerated}">
                                <span class="ai-badge">
                                    <i class="bi bi-robot me-1"></i>
                                    AI Pick
                                </span>
                            </c:if>
                            
                            <div class="position-relative">
                                <img src="${pageContext.request.contextPath}/${course.image}" 
                                     class="card-img-top" 
                                     alt="${course.name}" 
                                     style="height: 200px; object-fit: cover;">
                                <div class="position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-25 d-flex align-items-center justify-content-center opacity-0 hover-overlay">
                                    <a href="${pageContext.request.contextPath}/course?courseId=${course.idCourse}" 
                                       class="btn btn-light btn-lg">
                                        <i class="bi bi-play-fill me-2"></i>
                                        View Course
                                    </a>
                                </div>
                            </div>
                            
                            <div class="card-body d-flex flex-column">
                                <h5 class="card-title fw-bold">${course.name}</h5>
                                <p class="card-text text-muted flex-grow-1">${course.description}</p>
                                
                                <c:if test="${not empty course.teacherName}">
                                    <p class="text-muted small mb-2">
                                        <i class="bi bi-person-fill me-1"></i>
                                        Instructor: ${course.teacherName}
                                    </p>
                                </c:if>
                                
                                <div class="d-flex justify-content-between align-items-center mt-auto">
                                    <small class="text-muted">
                                        <i class="bi bi-trophy me-1"></i>
                                        Recommended #${status.index + 1}
                                    </small>
                                    <a href="${pageContext.request.contextPath}/course?courseId=${course.idCourse}" 
                                       class="btn btn-primary btn-sm">
                                        Learn More
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
        
        <!-- AI Assistant Integration -->
        <div class="mt-5">
            <div class="card border-0 shadow-sm">
                <div class="card-body text-center">
                    <h5 class="card-title">
                        <i class="bi bi-chat-dots-fill me-2"></i>
                        Need Personalized Guidance?
                    </h5>
                    <p class="card-text text-muted">
                        Chat with our AI assistant for customized learning advice and course recommendations
                    </p>
                    <button id="openChatbot" class="btn btn-gradient">
                        <i class="bi bi-robot me-2"></i>
                        Ask AI Assistant
                    </button>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Floating Chatbot Button -->
    <div class="chatbot-integration">
        <button id="chatbotToggle" class="btn btn-primary rounded-circle p-3 shadow">
            <i class="bi bi-robot fs-4"></i>
        </button>
    </div>
    
    <!-- Chatbot Modal -->
    <div class="modal fade" id="chatbotModal" tabindex="-1" aria-labelledby="chatbotModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title" id="chatbotModalLabel">
                        <i class="bi bi-robot me-2"></i>
                        AI Learning Assistant
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" style="height: 400px; overflow-y: auto;" id="chatMessages">
                    <div class="text-center text-muted">
                        <i class="bi bi-robot fs-1 mb-3 d-block"></i>
                        <p>Hello! I'm your AI learning assistant. Ask me about:</p>
                        <ul class="list-unstyled">
                            <li><i class="bi bi-check text-success me-2"></i>Course recommendations</li>
                            <li><i class="bi bi-check text-success me-2"></i>Programming help</li>
                            <li><i class="bi bi-check text-success me-2"></i>Learning paths</li>
                            <li><i class="bi bi-check text-success me-2"></i>Study guidance</li>
                        </ul>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="input-group">
                        <input type="text" class="form-control" id="chatInput" placeholder="Ask me anything about learning...">
                        <button class="btn btn-primary" type="button" id="sendChatMessage">
                            <i class="bi bi-send"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // Preference selection and dynamic recommendations
        let selectedPreferences = [];
        
        document.querySelectorAll('.preference-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const preference = this.dataset.preference;
                
                if (this.classList.contains('btn-primary')) {
                    this.classList.remove('btn-primary');
                    this.classList.add('btn-outline-primary');
                    selectedPreferences = selectedPreferences.filter(p => p !== preference);
                } else {
                    this.classList.remove('btn-outline-primary');
                    this.classList.add('btn-primary');
                    selectedPreferences.push(preference);
                }
            });
        });
        
        document.getElementById('refreshRecommendations').addEventListener('click', function() {
            updateRecommendations();
        });
        
        function updateRecommendations() {
            const loadingSpinner = document.querySelector('.loading-spinner');
            loadingSpinner.style.display = 'inline-block';
            
            fetch('${pageContext.request.contextPath}/ai-recommendations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    preferences: selectedPreferences.join(', '),
                    type: 'recommendation'
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    updateCourseCards(data.recommendations);
                } else {
                    console.error('Failed to get recommendations:', data.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
            })
            .finally(() => {
                loadingSpinner.style.display = 'none';
            });
        }
        
        function updateCourseCards(courses) {
            const container = document.getElementById('courseCards');
            container.innerHTML = '';
            
            courses.forEach((course, index) => {
                const courseCard = createCourseCard(course, index + 1);
                container.appendChild(courseCard);
            });
        }
        
        function createCourseCard(course, rank) {
            const col = document.createElement('div');
            col.className = 'col-lg-4 col-md-6 mb-4';
            
            col.innerHTML = `
                <div class="card course-card h-100 border-0 shadow">
                    <span class="ai-badge">
                        <i class="bi bi-robot me-1"></i>
                        AI Pick
                    </span>
                    <div class="position-relative">
                        <img src="${pageContext.request.contextPath}/${course.image}" 
                             class="card-img-top" 
                             alt="${course.name}" 
                             style="height: 200px; object-fit: cover;">
                    </div>
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title fw-bold">${course.name}</h5>
                        <p class="card-text text-muted flex-grow-1">${course.description}</p>
                        <div class="d-flex justify-content-between align-items-center mt-auto">
                            <small class="text-muted">
                                <i class="bi bi-trophy me-1"></i>
                                Recommended #${rank}
                            </small>
                            <a href="${pageContext.request.contextPath}/course?courseId=${course.id}" 
                               class="btn btn-primary btn-sm">
                                Learn More
                            </a>
                        </div>
                    </div>
                </div>
            `;
            
            return col;
        }
        
        // Chatbot functionality
        const chatbotModal = new bootstrap.Modal(document.getElementById('chatbotModal'));
        
        document.getElementById('chatbotToggle').addEventListener('click', function() {
            chatbotModal.show();
        });
        
        document.getElementById('openChatbot').addEventListener('click', function() {
            chatbotModal.show();
        });
        
        document.getElementById('sendChatMessage').addEventListener('click', function() {
            sendChatMessage();
        });
        
        document.getElementById('chatInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendChatMessage();
            }
        });
        
        function sendChatMessage() {
            const input = document.getElementById('chatInput');
            const message = input.value.trim();
            
            if (!message) return;
            
            const chatMessages = document.getElementById('chatMessages');
            
            // Add user message
            addMessageToChat('user', message);
            input.value = '';
            
            // Add typing indicator
            addTypingIndicator();
            
            // Send to AI
            fetch('${pageContext.request.contextPath}/chatbot', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: message,
                    type: 'recommendation'
                })
            })
            .then(response => response.json())
            .then(data => {
                removeTypingIndicator();
                addMessageToChat('ai', data.reply);
            })
            .catch(error => {
                removeTypingIndicator();
                addMessageToChat('ai', 'Sorry, I encountered an error. Please try again.');
                console.error('Chat error:', error);
            });
        }
        
        function addMessageToChat(sender, message) {
            const chatMessages = document.getElementById('chatMessages');
            const messageDiv = document.createElement('div');
            
            if (sender === 'user') {
                messageDiv.className = 'text-end mb-3';
                messageDiv.innerHTML = `
                    <div class="d-inline-block bg-primary text-white rounded p-2 max-width-70">
                        ${message}
                    </div>
                `;
            } else {
                messageDiv.className = 'text-start mb-3';
                messageDiv.innerHTML = `
                    <div class="d-inline-block bg-light rounded p-2 max-width-70">
                        <i class="bi bi-robot text-primary me-2"></i>
                        ${message}
                    </div>
                `;
            }
            
            chatMessages.appendChild(messageDiv);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
        
        function addTypingIndicator() {
            const chatMessages = document.getElementById('chatMessages');
            const typingDiv = document.createElement('div');
            typingDiv.id = 'typingIndicator';
            typingDiv.className = 'text-start mb-3';
            typingDiv.innerHTML = `
                <div class="d-inline-block bg-light rounded p-2">
                    <i class="bi bi-robot text-primary me-2"></i>
                    <span class="typing-dots">
                        <span></span>
                        <span></span>
                        <span></span>
                    </span>
                </div>
            `;
            
            chatMessages.appendChild(typingDiv);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
        
        function removeTypingIndicator() {
            const typingIndicator = document.getElementById('typingIndicator');
            if (typingIndicator) {
                typingIndicator.remove();
            }
        }
    </script>
    
    <style>
        .max-width-70 {
            max-width: 70%;
        }
        
        .typing-dots span {
            display: inline-block;
            width: 4px;
            height: 4px;
            background-color: #666;
            border-radius: 50%;
            margin-right: 2px;
            animation: typing 1.4s infinite;
        }
        
        .typing-dots span:nth-child(2) {
            animation-delay: 0.2s;
        }
        
        .typing-dots span:nth-child(3) {
            animation-delay: 0.4s;
        }
        
        @keyframes typing {
            0%, 60%, 100% {
                transform: translateY(0);
                opacity: 0.3;
            }
            30% {
                transform: translateY(-10px);
                opacity: 1;
            }
        }
        
        .hover-overlay {
            transition: opacity 0.3s ease;
        }
        
        .course-card:hover .hover-overlay {
            opacity: 1 !important;
        }
        
        .btn-gradient {
            background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;
        }
        
        .btn-gradient:hover {
            background: linear-gradient(45deg, #5a6fd8 0%, #6a4190 100%);
            color: white;
        }
    </style>
</body>
</html>
