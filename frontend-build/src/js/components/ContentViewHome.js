import { attachSharedStyles } from '../utils/DOMUtils.js';
import { store } from '../store/Store.js';

/**
 * * This component acts as the "Home" or "Landing Page" of our application.
 * It demonstrates how to interact with the global store (State Management)
 * by allowing the user to change their display name.
 */
class ContentViewHome extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    /**
     * Lifecycle method: Called when the component is added to the page
     */
    connectedCallback() {
        attachSharedStyles(this.shadowRoot);
        this.render();
        this.setupLogic();
    }

    /**
     * Renders the visual structure of the component
     */
    render() {
        this.shadowRoot.innerHTML += `
            <article class="card container">
                <h1>Welcome, <span id="name-display" style="color:var(--accent-pink)"></span></h1>
                
                <p>
                    This is the central control system for the robot units. 
                    Please identify yourself to proceed.
                </p>
                
                <div style="background: rgba(255,255,255,0.03); padding: 20px; border-radius: 8px; margin-top: 20px;">
                    <label for="nameInput" class="muted" style="display:block; margin-bottom:8px;">
                        Change Username (Global State)
                    </label>
                    
                    <div class="row">
                        <input id="nameInput" class="input" type="text" placeholder="Enter new name..." />
                        <button id="saveBtn" class="btn">Save</button>
                    </div>
                    
                    <p class="muted" style="font-size: 0.8rem; margin-top: 10px;">
                       ℹ️ Changes to the name are propagated immediately to the Header 
                       and other components via the Observer Pattern.
                    </p>
                </div>
            </article>
        `;
    }

    /**
     * Handles the interaction logic
     * Separating this from 'render' makes the code easier to read
     */
    setupLogic() {
        const nameDisplay = this.shadowRoot.getElementById('name-display');
        const input = this.shadowRoot.getElementById('nameInput');
        const btn = this.shadowRoot.getElementById('saveBtn');
        const state = store.getState();
        input.value = state ? state.username : '';

        this._unsub = store.subscribe((newState) => {
            nameDisplay.textContent = newState.username;
        });

        btn.addEventListener('click', () => {
            store.setUsername(input.value);
        });
    }
    
    /**
     * Lifecycle method: Called when the component is removed
     */
    disconnectedCallback() { 
        if (this._unsub) {
            this._unsub(); 
        }
    }
}

if(customElements.get('ontent-view-home')===undefined)
    customElements.define('ontent-view-home', ContentViewHome);