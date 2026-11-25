<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="../partials/head.jsp"/>

<section class="shell settings-section">
    <div class="settings-heading">
        <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>Account settings</p>
        <h1 class="section-title">Your profile</h1>
        <p class="section-lede">How you appear to the campus community, and how to reach your account.</p>
    </div>

    <div class="settings-grid">
        <jsp:include page="../partials/settings-nav.jsp"/>

        <div class="settings-content">
            <section class="panel">
                <div class="panel-heading">
                    <h2>Public profile</h2>
                    <span class="panel-note">Visible beside your broadcasts and comments</span>
                </div>

                <div class="avatar-block">
                    <ui:avatar user="${currentUser}" size="xl"/>
                    <div>
                        <p class="avatar-name"><c:out value="${currentUser.name}"/></p>
                        <p class="avatar-email"><c:out value="${currentUser.email}"/></p>

                        <c:if test="${not empty currentUser.avatarPath}">
                            <form method="post" action="${ctx}/settings/profile/avatar"
                                  data-confirm="Remove your profile photo?">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <button class="text-link text-link-danger" type="submit">Remove photo</button>
                            </form>
                        </c:if>
                    </div>
                </div>

                <form class="settings-form" method="post" action="${ctx}/settings/profile?_csrf=${csrfToken}"
                      enctype="multipart/form-data">
                    <input type="hidden" name="_csrf" value="${csrfToken}">

                    <div class="field">
                        <label for="name">Full name</label>
                        <input id="name" name="name" type="text" maxlength="255" required
                               value="<c:out value='${formName}'/>"
                               class="${not empty errors.name ? 'is-invalid' : ''}">
                        <c:if test="${not empty errors.name}">
                            <p class="field-error"><c:out value="${errors.name}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="email">Email address</label>
                        <input id="email" name="email" type="email" maxlength="255" required
                               value="<c:out value='${formEmail}'/>"
                               class="${not empty errors.email ? 'is-invalid' : ''}">
                        <c:if test="${not empty errors.email}">
                            <p class="field-error"><c:out value="${errors.email}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="photo">Profile photo</label>
                        <input id="photo" name="photo" type="file" accept="image/jpeg,image/png,image/webp"
                               class="${not empty errors.photo ? 'is-invalid' : ''}">
                        <p class="field-hint">JPG, PNG or WebP, up to 2 MB. A square image looks best.</p>
                        <c:if test="${not empty errors.photo}">
                            <p class="field-error"><c:out value="${errors.photo}"/></p>
                        </c:if>
                    </div>

                    <div class="form-actions">
                        <button class="btn btn-primary" type="submit">Save profile</button>
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

            <section class="panel panel-danger" aria-labelledby="delete-account-title">
                <div class="panel-heading">
                    <h2 id="delete-account-title">Delete account</h2>
                </div>
                <p class="panel-text">
                    Deleting your account removes your profile, every broadcast you have written and all of your comments and bookmarks.
                    This cannot be undone.
                </p>

                <c:if test="${not empty deleteErrors}">
                    <div class="notice notice-error" role="alert">
                        <c:forEach items="${deleteErrors}" var="entry">
                            <p><c:out value="${entry.value}"/></p>
                        </c:forEach>
                    </div>
                </c:if>

                <form class="settings-form" method="post" action="${ctx}/settings/account"
                      data-confirm="Delete your account and all of its content? This cannot be undone.">
                    <input type="hidden" name="_csrf" value="${csrfToken}">

                    <div class="field">
                        <label for="delete-password">Confirm with your password</label>
                        <input id="delete-password" name="password" type="password" autocomplete="current-password"
                               required placeholder="Your current password">
                    </div>

                    <button class="btn btn-danger" type="submit">Delete my account</button>
                </form>
            </section>
        </div>
    </div>
</section>

<jsp:include page="../partials/foot.jsp"/>
