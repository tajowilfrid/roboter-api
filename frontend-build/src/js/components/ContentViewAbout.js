import { attachSharedStyles } from '../utils/DOMUtils.js';
class ContentViewAbout extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    /**
     * Lifecycle method: Called when the component is added to the DOM
     */
    connectedCallback() {
        attachSharedStyles(this.shadowRoot);
        this.render();
    }

    /**
     * Since this content is static, we don't need any variables or parameters here.
     */
    render() {
        this.shadowRoot.innerHTML += `
            <article class="card">
                <h2>About the System</h2>
                
                <p>
                    This interface demonstrates a modern frontend architecture based on 
                    <strong>Vanilla Web Components</strong> (Native Browser Standards) without external frameworks.
                </p>
                
                <h3>Technical Highlights</h3>
                <ul style="color:var(--text-secondary); line-height:1.8;">
                    <li>
                        <strong>Shadow DOM Isolation:</strong> Encapsulation of HTML & Logic.
                    </li>
                    <li>
                        <strong>Central CSS Injection:</strong> Global styles are loaded dynamically.
                    </li>
                    <li>
                        <strong>Store Pattern:</strong> Reactive State-Management (Pub/Sub).
                    </li>
                    <li>
                        <strong>Modular API:</strong> Clean separation of backend calls.
                    </li>
                    <li>
                        <strong>Icons:</strong> SVG-based, no font dependencies.
                    </li>
                </ul>

                <h3 class="mt-1">Backend</h3>
                <p>Spring Boot REST API (HATEOAS compliant).</p>
                
                <div class="mono" style="background:rgba(0,0,0,0.3); padding:8px; border-radius:4px;">
                    Ver. 1.0.0-SNAPSHOT
                </div>
            </article>
        `;
    }
}

if(customElements.get('content-view-about')===undefined)
    customElements.define('content-view-about', ContentViewAbout);