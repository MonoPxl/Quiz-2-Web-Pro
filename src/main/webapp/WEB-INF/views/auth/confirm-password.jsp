<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="auth-section">
    <div class="auth-card">
        <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>One more step</p>
        <h1 class="auth-title">Confirm your password</h1>
        <p class="auth-lede">For your security, confirm your current password before changing it. This confirmation lasts for three hours.</p>

        <c:if test="${not empty errors}">
            <div class="notice notice-error" role="alert">
                <strong>That password did not match.</strong> Please try again.
            </div>
        </c:if>

        <form class="auth-form" method="post" action="${ctx}/confirm-password">
            <input type="hidden" name="_csrf" value="${csrfToken}">

            <div class="field">
                <label for="password">Current password</label>
                <input id="password" name="password" type="password" autocomplete="current-password" required
                       class="${not empty errors.password ? 'is-invalid' : ''}"
                       placeholder="Your current password" autofocus>
                <c:if test="${not empty errors.password}">
                    <p class="field-error"><c:out value="${errors.password}"/></p>
                </c:if>
            </div>

            <button class="btn btn-primary btn-block" type="submit">Confirm and continue</button>
        </form>

        <p class="auth-switch">
            <a href="${ctx}/settings/profile">Back to settings</a>
        </p>
    </div>

    <aside class="auth-aside" aria-hidden="true">
        <div class="auth-aside-card">
            <p class="hero-card-kicker">Why this step?</p>
            <strong>Passwords protect more than a profile.</strong>
            <p>Confirming first means someone using an unlocked device cannot quietly change your credentials.</p>
            <div class="auth-signal">
                <svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                    <circle cx="8" cy="20" r="2" fill="currentColor" stroke="none"/>
                    <path d="M14 13a10 10 0 0 1 0 14M19 8a17 17 0 0 1 0 24M24 3a24 24 0 0 1 0 34"/>
                </svg>
            </div>
        </div>
    </aside>
</section>

<jsp:include page="../partials/foot.jsp"/>
