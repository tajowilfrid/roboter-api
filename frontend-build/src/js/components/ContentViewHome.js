import { attachSharedStyles } from '../utils/DOMUtils.js';
import { store } from '../store/Store.js';

class ContentViewHome extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: 'open' });
  }

  connectedCallback() {
    attachSharedStyles(this.shadowRoot);

    this.shadowRoot.innerHTML += `
      <article class="card container">
        <h1>Willkommen, <span id="name-display" style="color:var(--accent-pink)"></span></h1>
        <p>Dies ist das zentrale Kontrollsystem für die Roboter-Einheiten. Bitte identifizieren Sie sich, um fortzufahren.</p>
        
        <div style="background: rgba(255,255,255,0.03); padding: 20px; border-radius: 8px; margin-top: 20px;">
            <label for="nameInput" class="muted" style="display:block; margin-bottom:8px;">Benutzername ändern (Global State)</label>
            <div class="row">
                <input id="nameInput" class="input" type="text" placeholder="Neuer Name..." />
                <button id="saveBtn" class="btn">Speichern</button>
            </div>
            <p class="muted" style="font-size: 0.8rem;">
               ℹ️ Änderungen am Namen werden per Observer-Pattern sofort an den Header und andere Komponenten propagiert.
            </p>
        </div>
      </article>
    `;

    const nameDisplay = this.shadowRoot.getElementById('name-display');
    const input = this.shadowRoot.getElementById('nameInput');
    const btn   = this.shadowRoot.getElementById('saveBtn');

    const state = store.getState();
    input.value = state ? state.username : '';

    this._unsub = store.subscribe((newState) => {
      nameDisplay.textContent = newState.username;
    });

    btn.addEventListener('click', () => store.setUsername(input.value));
  }
  
  disconnectedCallback() { this._unsub?.(); }
}

if (!customElements.get('content-view-home')) {
  customElements.define('content-view-home', ContentViewHome);
}