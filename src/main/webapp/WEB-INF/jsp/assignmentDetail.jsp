<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="navbar.jsp" />
<div class="container-fluid mt-4">
    <div class="row">
        <!-- Sidebar: Lecture and Assignment Name -->
        <div class="col-md-3 border-end">
            <h5 class="mt-4"><strong>${lecture.title}</strong></h5>
            <div class="mb-2">${assignment.name}</div>
        </div>
        <!-- Main Content: Assignment Details -->
        <div class="col-md-9">
            <h1 class="fw-bold">${assignment.name}</h1>
            <div class="mb-3">
                <strong>Due date:</strong>
                <span style="color:${assignment.dueDatePassed ? 'red' : 'green'};">
                    ${assignment.dueDate}
                </span>
            </div>
            <div class="mb-4">
                <strong>Content</strong>
                <div>${assignment.content}</div>
            </div>
            <div class="d-flex justify-content-between mt-5">
                <form method="post" action="${pageContext.request.contextPath}/assignments?assignmentId=${assignment.id}">
                    <button type="submit" class="btn ${assignment.submitted ? 'btn-secondary' : 'btn-primary'}" ${assignment.submitted ? 'disabled' : ''}>
                        Submit
                    </button>
                    <c:if test="${assignment.graded}">
                        <span class="ms-2">Score: ${assignment.score}</span>
                    </c:if>
                </form>
                <c:if test="${assignment.graded}">
                    <a href="${pageContext.request.contextPath}/continue?assignmentId=${assignment.id}" class="btn btn-success">
                        Continue
                    </a>
                </c:if>
            </div>
        </div>
    </div>
</div>