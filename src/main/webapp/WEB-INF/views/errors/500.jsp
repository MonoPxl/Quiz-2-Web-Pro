<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="shell message-section">
    <div class="message-card">
        <p class="message-code">500</p>
        <h1>Something went wrong on our side</h1>
        <p>The campus signal dropped for a moment. Please try again. If it keeps happening, let the site administrator know.</p>

        <div class="message-actions">
            <a class="btn btn-primary" href="${ctx}/">Back to the feed</a>
        </div>
    </div>
</section>

<jsp:include page="../partials/foot.jsp"/>
