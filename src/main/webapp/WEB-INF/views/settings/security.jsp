<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../partials/head.jsp"/>

<section class="shell settings-section">
    <div class="settings-heading">
        <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>Account settings</p>
        <h1 class="section-title">Password and security</h1>
        <p class="section-lede">Change the password used to sign in to myITS Broadcast.</p>
    </div>

    <div class="settings-grid">
        <jsp:include page="../partials/settings-nav.jsp"/>

        <div class="settings-content">
            <section class="panel">
                <div class="panel-heading">
                    <h2>Change password</h2>
                    <span class="panel-note">You confirmed your identity recently</span>
                </div>

                <c:if test="${not empty errors}">
                    <div class="notice notice-error" role="alert">
                        <strong>Please correct the fields below.</strong>
                    </div>
                </c:if>

                <form class="settings-form" method="post" action="${ctx}/settings/security">
                    <input type="hidden" name="_csrf" value="${csrfToken}">

                    <div class="field">
                        <label for="current_password">Current password</label>
                        <input id="current_password" name="current_password" type="password"
                               autocomplete="current-password" required
                               class="${not empty errors.currentPassword ? 'is-invalid' : ''}">
                        <c:if test="${not empty errors.currentPassword}">
                            <p class="field-error"><c:out value="${errors.currentPassword}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="password">New password</label>
                        <input id="password" name="password" type="password" autocomplete="new-password" required
                               class="${not empty errors.password ? 'is-invalid' : ''}"
                               placeholder="At least 8 characters">
                        <c:if test="${not empty errors.password}">
                            <p class="field-error"><c:out value="${errors.password}"/></p>
                        </c:if>
                        <p class="field-hint">Use at least 8 characters. Longer, mixed-character passwords are safer.</p>
                    </div>

                    <div class="field">
                        <label for="password_confirmation">Confirm new password</label>
                        <input id="password_confirmation" name="password_confirmation" type="password"
                               autocomplete="new-password" required placeholder="Repeat the new password">
                    </div>

                    <div class="form-actions">
                        <button class="btn btn-primary" type="submit">Update password</button>
                    </div>
                </form>
            </section>

            <section class="panel panel-quiet">
                <div class="panel-heading">
                    <h2>Session</h2>
                </div>
                <p class="panel-text">Signing out ends this session on the current device. You can sign back in at any time.</p>

                <form method="post" action="${ctx}/logout">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <button class="btn btn-quiet" type="submit">Log out</button>
                </form>
            </section>
        </div>
    </div>
</section>

<jsp:include page="../partials/foot.jsp"/>
