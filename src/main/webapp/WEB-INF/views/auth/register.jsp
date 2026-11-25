<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="auth-section">
    <div class="auth-card">
        <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>Join the conversation</p>
        <h1 class="auth-title">Create your account</h1>
        <p class="auth-lede">Your account works right away &mdash; no email verification step.</p>

        <c:if test="${not empty errors}">
            <div class="notice notice-error" role="alert">
                <strong>We could not create your account.</strong> Please correct the fields below.
            </div>
        </c:if>

        <form class="auth-form" method="post" action="${ctx}/register">
            <input type="hidden" name="_csrf" value="${csrfToken}">

            <div class="field">
                <label for="name">Full name</label>
                <input id="name" name="name" type="text" autocomplete="name" maxlength="255" required
                       value="<c:out value='${name}'/>"
                       class="${not empty errors.name ? 'is-invalid' : ''}"
                       placeholder="Your name as it appears on campus">
                <c:if test="${not empty errors.name}">
                    <p class="field-error"><c:out value="${errors.name}"/></p>
                </c:if>
            </div>

            <div class="field">
                <label for="email">Email address</label>
                <input id="email" name="email" type="email" autocomplete="email" maxlength="255" required
                       value="<c:out value='${email}'/>"
                       class="${not empty errors.email ? 'is-invalid' : ''}"
                       placeholder="you@student.its.ac.id">
                <c:if test="${not empty errors.email}">
                    <p class="field-error"><c:out value="${errors.email}"/></p>
                </c:if>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input id="password" name="password" type="password" autocomplete="new-password" required
                       class="${not empty errors.password ? 'is-invalid' : ''}"
                       placeholder="At least 8 characters">
                <c:if test="${not empty errors.password}">
                    <p class="field-error"><c:out value="${errors.password}"/></p>
                </c:if>
                <p class="field-hint">Use at least 8 characters. Longer, mixed-character passwords are safer.</p>
            </div>

            <div class="field">
                <label for="password_confirmation">Confirm password</label>
                <input id="password_confirmation" name="password_confirmation" type="password"
                       autocomplete="new-password" required placeholder="Repeat your password">
            </div>

            <button class="btn btn-primary btn-block" type="submit">Create account</button>
        </form>

        <p class="auth-switch">
            Already have an account?
            <a href="${ctx}/login">Log in</a>
        </p>
    </div>

    <aside class="auth-aside" aria-hidden="true">
        <div class="auth-aside-card">
            <p class="hero-card-kicker">Built for campus</p>
            <strong>Announcements, events and opportunities in one calm feed.</strong>
            <p>Publish updates, discuss them with the community, and bookmark what you want to come back to.</p>
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
