/* myITS Broadcast — small progressive-enhancement layer.
   Everything here is optional: every form and link works without JavaScript. */

(function () {
    "use strict";

    var csrfMeta = document.querySelector('meta[name="csrf-token"]');

    function csrfToken() {
        if (csrfMeta) {
            return csrfMeta.getAttribute("content");
        }

        var input = document.querySelector('input[name="_csrf"]');

        return input ? input.value : "";
    }

    function toast(message, kind) {
        var stack = document.querySelector(".toast-stack");

        if (!stack) {
            return;
        }

        var element = document.createElement("div");
        element.className = "toast " + (kind === "error" ? "toast-error" : "toast-success");
        element.setAttribute("role", kind === "error" ? "alert" : "status");

        var text = document.createElement("p");
        text.textContent = message;
        element.appendChild(text);

        var close = document.createElement("button");
        close.type = "button";
        close.className = "toast-close";
        close.setAttribute("aria-label", "Dismiss notification");
        close.innerHTML = "&times;";
        close.addEventListener("click", function () {
            dismiss(element);
        });
        element.appendChild(close);

        stack.appendChild(element);
        window.setTimeout(function () {
            dismiss(element);
        }, 6000);
    }

    function dismiss(element) {
        if (!element || element.classList.contains("is-leaving")) {
            return;
        }

        element.classList.add("is-leaving");
        window.setTimeout(function () {
            element.remove();
        }, 220);
    }

    document.querySelectorAll("[data-toast]").forEach(function (element) {
        var close = element.querySelector("[data-toast-close]");

        if (close) {
            close.addEventListener("click", function () {
                dismiss(element);
            });
        }

        window.setTimeout(function () {
            dismiss(element);
        }, 7000);
    });

    /* Confirmation dialogs for destructive actions. */
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.confirm(form.getAttribute("data-confirm"))) {
                event.preventDefault();
            }
        });
    });

    /* Bookmark toggle without a reload; falls back to the normal POST. */
    document.querySelectorAll("[data-bookmark-form]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.fetch || !window.FormData) {
                return;
            }

            event.preventDefault();

            var button = form.querySelector("[data-bookmark-button]");
            var label = form.querySelector("[data-bookmark-label]");
            var postId = form.getAttribute("data-post-id");
            var body = new FormData(form);

            if (button) {
                button.disabled = true;
            }

            fetch(form.action, {
                method: "POST",
                body: body,
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                    "Accept": "application/json",
                    "X-CSRF-Token": csrfToken()
                },
                credentials: "same-origin"
            }).then(function (response) {
                if (response.status === 401) {
                    var context = document.querySelector('meta[name="app-context"]');
                    var base = context ? context.getAttribute("content") : "";
                    window.location.href = (base || "") + "/login";
                    return null;
                }

                if (!response.ok) {
                    throw new Error("request failed");
                }

                return response.json();
            }).then(function (payload) {
                if (!payload || !button) {
                    return;
                }

                var active = payload.bookmarked === true;
                button.classList.toggle("is-active", active);
                button.setAttribute("aria-pressed", active ? "true" : "false");

                if (label) {
                    label.textContent = active ? "Bookmarked" : "Bookmark";
                }

                var icon = button.querySelector("svg");

                if (icon) {
                    icon.setAttribute("fill", active ? "currentColor" : "none");
                }

                document.querySelectorAll('[data-bookmark-count]').forEach(function (counter) {
                    var next = Math.max(0, parseInt(counter.textContent, 10) + (active ? 1 : -1));
                    counter.textContent = String(next);
                });

                if (postId) {
                    document.querySelectorAll('[data-bookmark-stat="' + postId + '"]').forEach(function (counter) {
                        var next = Math.max(0, parseInt(counter.textContent, 10) + (active ? 1 : -1));
                        counter.textContent = String(next);
                        counter.classList.toggle("is-active", active);
                    });
                }

                toast(payload.message || (active ? "Post bookmarked." : "Bookmark removed."), "success");
            }).catch(function () {
                form.submit();
            }).finally(function () {
                if (button) {
                    button.disabled = false;
                }
            });
        });
    });

    /* Show event-only fields only while "Event" is selected. */
    var eventFields = document.querySelector("[data-event-fields]");

    if (eventFields) {
        var typeRadios = document.querySelectorAll('input[name="type"]');

        function syncEventFields() {
            var selected = document.querySelector('input[name="type"]:checked');
            var isEvent = selected && selected.value === "event";

            eventFields.hidden = !isEvent;

            if (!isEvent) {
                eventFields.querySelectorAll("input").forEach(function (input) {
                    input.value = "";
                });
            }
        }

        typeRadios.forEach(function (radio) {
            radio.addEventListener("change", syncEventFields);
        });
    }

    /* Keep the selected visibility card highlighted. */
    document.querySelectorAll(".choice input").forEach(function (radio) {
        radio.addEventListener("change", function () {
            document.querySelectorAll(".choice").forEach(function (choice) {
                choice.classList.toggle("is-selected", choice.contains(radio) && radio.checked);
            });
        });
    });

    /* Local cover-image preview, validated by type and size before upload. */
    var coverInput = document.querySelector("[data-cover-input]");
    var coverPreview = document.querySelector("[data-cover-preview]");

    if (coverInput && coverPreview) {
        coverInput.addEventListener("change", function () {
            var file = coverInput.files && coverInput.files[0];

            if (!file) {
                return;
            }

            if (["image/jpeg", "image/png", "image/webp"].indexOf(file.type) === -1 || file.size > 2 * 1024 * 1024) {
                toast("Choose a JPG, PNG or WebP image no larger than 2 MB.", "error");
                coverInput.value = "";
                return;
            }

            var reader = new FileReader();
            reader.addEventListener("load", function () {
                coverPreview.classList.remove("is-empty");
                coverPreview.innerHTML = "";

                var image = document.createElement("img");
                image.src = String(reader.result);
                image.alt = "Selected cover image preview";
                image.setAttribute("data-cover-image", "");
                coverPreview.appendChild(image);
            });
            reader.readAsDataURL(file);
        });
    }

    /* The compact header stays out of the way until the hero scrolls past,
       then slides in from the top like the legacy build. */
    var sticky = document.querySelector("[data-sticky-nav]");

    if (sticky) {
        var toggleSticky = function () {
            sticky.classList.toggle("is-stuck", window.scrollY > 180);
        };

        toggleSticky();
        window.addEventListener("scroll", toggleSticky, { passive: true });
    }

    /* Escape closes an open inline comment editor by returning to the post. */
    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            var textarea = document.querySelector(".comment-edit textarea");

            if (textarea) {
                var cancel = textarea.closest("form").querySelector("a.btn");

                if (cancel) {
                    window.location.href = cancel.href;
                }
            }
        }
    });
})();
