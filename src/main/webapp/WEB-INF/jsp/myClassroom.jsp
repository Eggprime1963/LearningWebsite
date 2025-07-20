<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
    <meta charset="UTF-8">
    <title>My Classroom</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/classroom.css"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
    <jsp:include page="navbar.jsp" />
    <div class="container-fluid mt-4">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-3">
                <div class="card p-3">
                    <h5 class="mb-3">Navigation</h5>
                    <ul class="nav flex-column">
                        <li class="nav-item"><a class="nav-link ${section == 'courses' ? 'active' : ''}" href="?section=courses">Courses</a></li>
                        <li class="nav-item"><a class="nav-link ${section == 'assignments' ? 'active' : ''}" href="?section=assignments">Assignments</a></li>
                        <li class="nav-item"><a class="nav-link ${section == 'lectures' ? 'active' : ''}" href="?section=lectures">Lectures</a></li>
                    </ul>
                </div>
            </div>
            <!-- Main Content -->
            <div class="col-md-9">
                <h2 class="mb-4">My ${section == 'courses' ? 'Courses' : section == 'assignments' ? 'Assignments' : 'Lectures'}</h2>
                <c:if test="${canCrud}">
                    <button class="btn btn-primary mb-3" data-bs-toggle="modal" data-bs-target="#addModal">
                        Add ${section == 'courses' ? 'Course' : section == 'assignments' ? 'Assignment' : 'Lecture'}
                    </button>
                </c:if>
                <c:if test="${section != 'courses'}">
                    <form method="get" action="myClassroom" class="mb-3">
                        <input type="hidden" name="section" value="${section}">
                        <label for="courseSelect" class="form-label">Choose Course:</label>
                        <select name="courseId" id="courseSelect" class="form-select" onchange="this.form.submit()">
                            <c:forEach var="c" items="${courses}">
                                <option value="${c.idCourse}" <c:if test="${selectedCourse != null && selectedCourse.idCourse == c.idCourse}">selected</c:if>>
                                    ${c.name}
                                </option>
                            </c:forEach>
                        </select>
                    </form>
                </c:if>
                <div class="row g-4">
                    <c:choose>
                        <c:when test="${section == 'courses'}">
                            <c:forEach var="c" items="${courses}">
                                <div class="col-md-6 col-lg-4">
                                    <div class="card h-100 shadow-sm rounded-4">
                                        <img src="${pageContext.request.contextPath}/img/${c.thumbnail}" class="card-img-top rounded-top-4" alt="Course Thumbnail" style="height:180px;object-fit:cover;">
                                        <div class="card-body d-flex flex-column">
                                            <h5 class="card-title">${c.name}</h5>
                                            <p class="card-text">${c.description}</p>
                                            <div class="mt-auto">
                                                <c:if test="${section == 'courses'}">
                                                    <div class="row g-4">
                                                        <c:forEach var="course" items="${courses}">
                                                            <div class="col-md-6 col-lg-4">
                                                                <a href="${pageContext.request.contextPath}/lectures?courseId=${course.idCourse}" class="btn btn-primary">
                                                                    View Course
                                                                </a>
                                                            </div>
                                                        </c:forEach>
                                                    </div>
                                                </c:if>
                                                <c:if test="${canCrud}">
                                                    <button class="btn btn-warning btn-sm mb-2" onclick="openEditModal('${c.idCourse}', '${c.name}', '${c.description}')">Edit</button>
                                                    <form action="${pageContext.request.contextPath}/courses" method="post" style="display:inline;">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="courseId" value="${c.idCourse}">
                                                        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Are you sure?')">Delete</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:when test="${section == 'assignments'}">
                            <c:forEach var="a" items="${assignments}">
                                <div class="col-md-6 col-lg-4">
                                    <div class="card h-100 shadow-sm rounded-4">
                                        <img src="${pageContext.request.contextPath}/img/assignment.png" class="card-img-top rounded-top-4" alt="Assignment" style="height:180px;object-fit:cover;">
                                        <div class="card-body d-flex flex-column">
                                            <h5 class="card-title">${a.title}</h5>
                                            <p class="card-text">Due: ${a.dueDate}<br>Status: ${a.status}</p>
                                            <div class="mt-auto">
                                                <c:if test="${canCrud}">
                                                    <!-- Add edit/delete buttons as needed -->
                                                    <button class="btn btn-warning btn-sm mb-2">Edit</button>
                                                    <form action="${pageContext.request.contextPath}/assignments" method="post" style="display:inline;">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="assignmentId" value="${a.idAssignment}">
                                                        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Are you sure?')">Delete</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="l" items="${lectures}">
                                <div class="col-md-6 col-lg-4">
                                    <div class="card h-100 shadow-sm rounded-4">
                                        <img src="${pageContext.request.contextPath}/img/lecture.png" class="card-img-top rounded-top-4" alt="Lecture" style="height:180px;object-fit:cover;">
                                        <div class="card-body d-flex flex-column">
                                            <h5 class="card-title">${l.title}</h5>
                                            <p class="card-text">Video: <a href="${l.videoUrl}" target="_blank">${l.videoUrl}</a></p>
                                            <div class="mt-auto">
                                                <c:if test="${canCrud}">
                                                    <!-- Add edit/delete buttons as needed -->
                                                    <button class="btn btn-warning btn-sm mb-2">Edit</button>
                                                    <form action="${pageContext.request.contextPath}/lectures" method="post" style="display:inline;">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="lectureId" value="${l.idLecture}">
                                                        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Are you sure?')">Delete</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <!-- Added section for course buttons -->
            </div>
        </div>
    </div>

    <!-- Add Modal (fixed for all sections) -->
    <div class="modal fade" id="addModal" tabindex="-1" aria-labelledby="addModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/${section}" method="post" class="modal-content">
            <input type="hidden" name="action" value="create">
            <div class="modal-header">
                <h5 class="modal-title" id="addModalLabel">Add ${section == 'courses' ? 'Course' : section == 'assignments' ? 'Assignment' : 'Lecture'}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <c:choose>
                    <c:when test="${section == 'courses'}">
                        <div class="mb-3">
                            <label>Course Name</label>
                            <input type="text" class="form-control" name="name" required>
                        </div>
                        <div class="mb-3">
                            <label>Description</label>
                            <textarea class="form-control" name="description" required></textarea>
                        </div>
                        <div class="mb-3">
                            <label>Thumbnail URL</label>
                            <input type="text" class="form-control" name="thumbnail">
                        </div>
                    </c:when>
                    <c:when test="${section == 'assignments'}">
                        <div class="mb-3">
                            <label>Title</label>
                            <input type="text" class="form-control" name="title" required>
                        </div>
                        <div class="mb-3">
                            <label>Due Date</label>
                            <input type="datetime-local" class="form-control" name="dueDate" required>
                        </div>
                        <div class="mb-3">
                            <label>Status</label>
                            <select class="form-control" name="status">
                                <option value="not yet">Not Yet</option>
                                <option value="in progress">In Progress</option>
                                <option value="ended">Ended</option>
                            </select>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="mb-3">
                            <label>Title</label>
                            <input type="text" class="form-control" name="title" required>
                        </div>
                        <div class="mb-3">
                            <label>Video URL</label>
                            <input type="text" class="form-control" name="videoUrl" required>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-success">Save</button>
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
            </div>
        </form>
      </div>
    </div>

    <script>
    function openEditModal(id, name, description) {
        document.getElementById('editId').value = id;
        document.getElementById('editName').value = name;
        document.getElementById('editDescription').value = description;
        new bootstrap.Modal(document.getElementById('editModal')).show();
    }
    </script>
</body>
</html>
