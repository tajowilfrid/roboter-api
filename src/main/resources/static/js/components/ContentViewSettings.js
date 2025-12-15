import { attachSharedStyles } from '../utils/DOMUtils.js';
import { store } from '../store/Store.js';
import { api } from '../api/RobotApiService.js';

/**
 * * This component renders a simple form to configure global settings.
 * It allows the user to change the Operator Name and the Target Robot ID.
 */
class ContentViewSettings extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    /**
     * Lifecycle method: Called when the component is added to the page.
     */
    connectedCallback() {
        attachSharedStyles(this.shadowRoot);
        this.render();
        this.setupLogic();
    }

    /**
     * Helper method to handle input values and button clicks.
     * Separating this from connectedCallback makes the code cleaner.
     */
    setupLogic() {
        const state = store.getState();

        const userIn = this.shadowRoot.getElementById('in-user');
        const robotIn = this.shadowRoot.getElementById('in-robot');
        const saveBtn = this.shadowRoot.getElementById('btn-save');
        const resetBtn = this.shadowRoot.getElementById('btn-reset');

        userIn.value = state.username;
        robotIn.value = state.robotId;

        saveBtn.addEventListener('click', () => {
            store.setUsername(userIn.value);
            store.setRobotId(robotIn.value);
            
            alert('Settings saved successfully.');
        });

        resetBtn.addEventListener('click', async () => {
            if(confirm("Are you sure you want to reset the system? All progress will be lost.")) {
                try {
                    await api.reset();
                    alert("System has been reset!");
                    window.location.reload(); 
                } catch(e) {
                    alert("Error during reset: " + e.message);
                }
            }
        });
    }

    render() {
        this.shadowRoot.innerHTML += `
            <article class="card">
                <h2>Configuration</h2>
                
                <div class="mb-1">
                    <label class="data-label">Operator Name</label>
                    <input id="in-user" class="input" type="text" placeholder="Enter name..." />
                </div>

                <div class="mb-1">
                    <label class="data-label">Target Robot ID</label>
                    <input id="in-robot" class="input" type="text" placeholder="e.g. r1" />
                </div>

                <button id="btn-save" class="btn mt-1">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2zM7 3v4h8V3H7zm0 16h10v-8H7v8z"/>
                    </svg>
                    Save Settings
                </button>

                <div style="margin-top: 2rem; padding-top: 1rem; border-top: 1px solid var(--border-color);">
                    <h3 style="color: var(--status-danger)">Danger Zone</h3>
                    <p class="muted">Resets all robots to position (0,0) and 100% energy.</p>
                    
                    <button id="btn-reset" class="btn" style="border-color: var(--status-danger); color: var(--status-danger);">
                        System Reset
                    </button>
                </div>
            </article>
        `;
    }
}

if(customElements.get('content-view-settings')===undefined)
    customElements.define('content-view-settings', ContentViewSettings);