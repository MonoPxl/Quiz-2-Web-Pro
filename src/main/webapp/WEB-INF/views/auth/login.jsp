<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="auth-section">
    <div class="auth-card">
        <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>Welcome back</p>
        <h1 class="auth-title">Log in to myITS Broadcast</h1>
        <p class="auth-lede">Pick up the campus conversation where you left it.</p>

        <c:if test="${not empty errors}">
            <div class="notice notice-error" role="alert">
                <strong>We could not sign you in.</strong> Check the details below and try again.
            </div>
        </c:if>

        <form class="auth-form" method="post" action="${ctx}/login">
            <input type="hidden" name="_csrf" value="${csrfToken}">

            <div class="field">
                <label for="email">Email address</label>
                <input id="email" name="email" type="email" autocomplete="email" required
                       value="<c:out value='${email}'/>"
                       class="${not empty errors.email ? 'is-invalid' : ''}"
                       placeholder="you@student.its.ac.id">
                <c:if test="${not empty errors.email}">
                    <p class="field-error"><c:out value="${errors.email}"/></p>
                </c:if>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input id="password" name="password" type="password" autocomplete="current-password" required
                       class="${not empty errors.password ? 'is-invalid' : ''}"
                       placeholder="Your password">
                <c:if test="${not empty errors.password}">
                    <p class="field-error"><c:out value="${errors.password}"/></p>
                </c:if>
            </div>

            <label class="check">
                <input type="checkbox" name="remember" value="1">
                <span>Keep me signed in on this device</span>
            </label>

            <button class="btn btn-primary btn-block" type="submit">Log in</button>
        </form>

        <p class="auth-switch">
            New to myITS Broadcast?
            <a href="${ctx}/register">Create an account</a>
        </p>
    </div>

    <aside class="auth-aside" aria-hidden="true">
        <div class="auth-aside-card">
            <p class="hero-card-kicker">The campus signal</p>
            <strong>One place for every announcement and event.</strong>
            <p>Follow what matters, join the discussion, and keep the broadcasts you want to revisit.</p>
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
