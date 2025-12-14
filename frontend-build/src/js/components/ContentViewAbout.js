import { attachSharedStyles } from '../utils/DOMUtils.js';

/**
 * * A simple static view that displays information about the project architecture
 * It demonstrates that not every component needs complex logic or state subscriptions
 */
class ContentViewAbout extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    connectedCallback() {
        attachSharedStyles(this.shadowRoot);

        this.render();
    }

    render() {
        this.shadowRoot.innerHTML += `
            <article class="card">
                <h2>Über das System</h2>
                
                <p>
                    Dieses Interface demonstriert eine moderne Frontend-Architektur auf Basis von 
                    <strong>Vanilla Web Components</strong> (Native Browser Standards) ohne externe Frameworks.
                </p>
                
                <h3>Technische Highlights</h3>
                <ul style="color:var(--text-secondary); line-height:1.8;">
                    <li>
                        <strong>Shadow DOM Isolation:</strong> Kapselung von HTML & Logik.
                    </li>
                    <li>
                        <strong>Central CSS Injection:</strong> Globale Styles (style.css) werden dynamisch geladen.
                    </li>
                    <li>
                        <strong>Store Pattern:</strong> Reatives State-Management (Pub/Sub).
                    </li>
                    <li>
                        <strong>Modulare API:</strong> Saubere Trennung von Backend-Calls in RobotApiService.js.
                    </li>
                    <li>
                        <strong>Icons:</strong> SVG-basiert, keine Font-Abhängigkeiten.
                    </li>
                </ul>

                <h3 class="mt-1">Backend</h3>
                <p>Spring Boot REST API (HATEOAS konform) Level 3.</p>
                
                <div class="mono" style="background:rgba(0,0,0,0.3); padding:8px; border-radius:4px;">
                    Ver. 1.0.0-SNAPSHOT
                </div>
            </article>
        `;
    }
}

customElements.define('content-view-about', ContentViewAbout);