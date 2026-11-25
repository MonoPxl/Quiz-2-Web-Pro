<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="shell message-section">
    <div class="message-card">
        <p class="message-code">403</p>
        <h1>You cannot open this page</h1>
        <p>
            <c:choose>
                <c:when test="${empty errorMessage}">
                    Your account does not have permission to view this page, or the form you submitted has expired.
                </c:when>
                <c:otherwise><c:out value="${errorMessage}"/></c:otherwise>
            </c:choose>
        </p>

        <div class="message-actions">
            <a class="btn btn-primary" href="${ctx}/">Back to the feed</a>
            <a class="btn btn-quiet" href="${ctx}/dashboard">My broadcast</a>
        </div>
    </div>
</section>

<jsp:include page="../partials/foot.jsp"/>
