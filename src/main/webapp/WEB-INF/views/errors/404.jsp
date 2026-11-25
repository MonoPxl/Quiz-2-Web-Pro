<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="shell message-section">
    <div class="message-card">
        <p class="message-code">404</p>
        <h1>This signal is off the air</h1>
        <p>The broadcast you are looking for may have been removed, renamed, or never published.</p>

        <div class="message-actions">
            <a class="btn btn-primary" href="${ctx}/">Back to the feed</a>
            <c:if test="${not empty currentUser}">
                <a class="btn btn-quiet" href="${ctx}/dashboard">My broadcast</a>
            </c:if>
        </div>
    </div>
</section>

<jsp:include page="../partials/foot.jsp"/>
