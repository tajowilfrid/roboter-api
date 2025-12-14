/**
 * * Collection of helper functions to manipulate the DOM.
 * Mainly used for navigation (view switching) and handling Shadow DOM styling.
 */

export function showView(tagName) {
    const viewContainer = document.getElementById('view');

    // Replace the HTML string to render the new Web Component.
    viewContainer.innerHTML = `<${tagName}></${tagName}>`;

    window.scrollTo(0, 0);
}

/**
 * Injects the global 'style.css' into a Web Component's Shadow DOM
 */
export function attachSharedStyles(shadowRoot) {
    if (shadowRoot.querySelector('link[data-shared-css]')) {
        return;
    }

    const link = document.createElement('link');
    link.setAttribute('rel', 'stylesheet');
    link.setAttribute('href', '/css/style.css'); 
    link.setAttribute('data-shared-css', 'true');

    shadowRoot.prepend(link);
}