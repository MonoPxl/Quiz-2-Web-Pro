<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light">
    <meta name="description" content="Announcements, events and opportunities from across the ITS campus community.">
    <meta name="csrf-token" content="${csrfToken}">
    <meta name="app-context" content="${ctx}">
    <title><c:out value="${empty pageTitle ? 'myITS Broadcast' : pageTitle}"/></title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
    <link rel="icon" href="${ctx}/assets/img/signal.svg" type="image/svg+xml">
</head>
<body id="top">
<a class="skip-link" href="#main">Skip to content</a>

<c:choose>
    <c:when test="${layout == 'home'}">
        <header class="hero">
            <div class="hero-glow" aria-hidden="true"></div>
            <div class="shell hero-top">
                <a class="brand brand-light" href="${ctx}/" aria-label="myITS Broadcast home">
                    <span class="brand-mark" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                            <circle cx="5" cy="12" r="1.7" fill="currentColor" stroke="none"/>
                            <path d="M9 8.5a5 5 0 0 1 0 7M12 5.5a9 9 0 0 1 0 13M15 2.5a13 13 0 0 1 0 19"/>
                        </svg>
                    </span>
                    <span class="brand-word">myITS <span>Broadcast</span></span>
                </a>

                <div class="header-nav-desktop">
                    <jsp:include page="/WEB-INF/views/partials/nav-links.jsp"/>
                </div>

                <div class="header-account">
                    <jsp:include page="/WEB-INF/views/partials/account-controls.jsp"/>
                </div>
            </div>

            <div class="shell header-mobile-nav">
                <jsp:include page="/WEB-INF/views/partials/nav-links.jsp"/>
            </div>

            <div class="shell hero-grid">
                <div class="hero-copy">
                    <p class="eyebrow eyebrow-light"><span class="eyebrow-dot" aria-hidden="true"></span>The campus signal, in one place</p>
                    <h1>One campus.<br><em>Every signal.</em></h1>
                    <p class="hero-lede">A calmer, clearer place to discover announcements and events, share what matters, and stay connected to the ITS community.</p>
                    <div class="hero-meta">
                        <span><b aria-hidden="true"></b> Announcements</span>
                        <span><b aria-hidden="true"></b> Events</span>
                        <span><b aria-hidden="true"></b> Campus opportunities</span>
                    </div>
                </div>

                <div class="hero-art" aria-hidden="true">
                    <div class="hero-orbit"></div>
                    <div class="hero-core">
                        <svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                            <circle cx="8" cy="20" r="2" fill="currentColor" stroke="none"/>
                            <path d="M14 13a10 10 0 0 1 0 14M19 8a17 17 0 0 1 0 24M24 3a24 24 0 0 1 0 34"/>
                        </svg>
                    </div>
                    <div class="hero-card hero-card-one">
                        <span class="hero-card-kicker">Student activity</span>
                        <strong>Open Recruitment UKM Robotika ITS</strong>
                        <span>Announcement / Organisasi</span>
                    </div>
                    <div class="hero-card hero-card-two">
                        <span class="hero-card-kicker">Upcoming event</span>
                        <strong>AI for Sustainable Maritime Industry</strong>
                        <span>Event / Kegiatan Kampus</span>
                    </div>
                    <svg class="hero-wave" viewBox="0 0 700 60" fill="none">
                        <path d="M0 30H90C125 30 126 7 160 7S195 53 230 53 265 7 300 7 335 53 370 53 405 7 440 7 475 53 510 53 545 30 580 30H700" stroke="#91BAFF" stroke-width="2"/>
                        <path d="M0 43H80C115 43 125 22 160 22S205 44 240 44 275 22 310 22 345 44 380 44 415 22 450 22 485 44 520 44 550 43 580 43H700" stroke="#00AABA" stroke-width="1" opacity=".7"/>
                    </svg>
                </div>
            </div>
        </header>

        <div class="sticky-shell" data-sticky-nav>
            <header class="site-header site-header-compact">
                <div class="shell header-bar">
                    <a class="brand brand-light" href="${ctx}/" aria-label="myITS Broadcast home">
                        <span class="brand-mark" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                                <circle cx="5" cy="12" r="1.7" fill="currentColor" stroke="none"/>
                                <path d="M9 8.5a5 5 0 0 1 0 7M12 5.5a9 9 0 0 1 0 13M15 2.5a13 13 0 0 1 0 19"/>
                            </svg>
                        </span>
                        <span class="brand-word">myITS <span>Broadcast</span></span>
                    </a>

                    <div class="header-nav-desktop">
                        <jsp:include page="/WEB-INF/views/partials/nav-links.jsp"/>
                    </div>

                    <div class="header-account">
                        <jsp:include page="/WEB-INF/views/partials/account-controls.jsp"/>
                    </div>
                </div>

                <div class="shell header-mobile-nav">
                    <jsp:include page="/WEB-INF/views/partials/nav-links.jsp"/>
                </div>
            </header>
        </div>
    </c:when>

    <c:when test="${layout == 'auth'}">
        <header class="site-header site-header-compact site-header-auth">
            <div class="shell header-bar">
                <a class="brand brand-light" href="${ctx}/" aria-label="myITS Broadcast home">
                    <span class="brand-mark" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                            <circle cx="5" cy="12" r="1.7" fill="currentColor" stroke="none"/>
                            <path d="M9 8.5a5 5 0 0 1 0 7M12 5.5a9 9 0 0 1 0 13M15 2.5a13 13 0 0 1 0 19"/>
                        </svg>
                    </span>
                    <span class="brand-word">myITS <span>Broadcast</span></span>
                </a>

                <a class="header-back-link" href="${ctx}/">Back to feed</a>
            </div>
        </header>
    </c:when>

    <c:otherwise>
        <header class="site-header site-header-compact">
            <div class="shell header-bar">
                <a class="brand brand-light" href="${ctx}/" aria-label="myITS Broadcast home">
                    <span class="brand-mark" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                            <circle cx="5" cy="12" r="1.7" fill="currentColor" stroke="none"/>
                            <path d="M9 8.5a5 5 0 0 1 0 7M12 5.5a9 9 0 0 1 0 13M15 2.5a13 13 0 0 1 0 19"/>
                        </svg>
                    </span>
                    <span class="brand-word">myITS <span>Broadcast</span></span>
                </a>

                <div class="header-nav-desktop">
                    <jsp:include page="/WEB-INF/views/partials/nav-links.jsp"/>
                </div>

                <div class="header-account">
                    <jsp:include page="/WEB-INF/views/partials/account-controls.jsp"/>
                </div>
            </div>

            <div class="shell header-mobile-nav">
                <jsp:include page="/WEB-INF/views/partials/nav-links.jsp"/>
            </div>
        </header>
    </c:otherwise>
</c:choose>

<div class="toast-stack" aria-live="polite" aria-atomic="false">
    <c:if test="${not empty statusMessage}">
        <div class="toast toast-success" data-toast role="status">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-sm" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"/>
            </svg>
            <p><c:out value="${statusMessage}"/></p>
            <button type="button" class="toast-close" data-toast-close aria-label="Dismiss notification">&times;</button>
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="toast toast-error" data-toast role="alert">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-sm" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0 3.75h.008v.008H12v-.008ZM21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"/>
            </svg>
            <p><c:out value="${errorMessage}"/></p>
            <button type="button" class="toast-close" data-toast-close aria-label="Dismiss notification">&times;</button>
        </div>
    </c:if>
</div>

<main id="main" class="site-main">
