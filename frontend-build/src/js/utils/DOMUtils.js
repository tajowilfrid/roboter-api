import globalStyles from '../../css/style.css?inline';

export function showView(tagName) {
    const viewContainer = document.getElementById('view');
    viewContainer.innerHTML = `<${tagName}></${tagName}>`;
    window.scrollTo(0, 0);
}

export function attachSharedStyles(shadowRoot) {
    if (shadowRoot.querySelector('style[data-shared-css]')) return;

    const style = document.createElement('style');
    style.setAttribute('data-shared-css', 'true');
    style.textContent = globalStyles;

    shadowRoot.prepend(style);
}