import { attachSharedStyles, showView } from '../utils/DOMUtils.js';
import { store } from '../store/Store.js';

/**
 * * Renders the top navigation bar and the user status
 * It listens to the global store to display the current operator name
 */
class HeaderView extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    /**
     * Called when the element is added to the DOM.
     * This is where we setup styles, render content, and attach listeners.
     */
    connectedCallback() {
        attachSharedStyles(this.shadowRoot);

        this.render();

        this.unsub = store.subscribe(state => {
            const userEl = this.shadowRoot.getElementById('user-display');
            if (userEl) {
                userEl.textContent = `OP: ${state.username}`;
            }
        });

        this.setupNavigation();
    }

    disconnectedCallback() {
        if (this.unsub) {
            this.unsub();
        }
    }

    /**
     * Handles the click events on the navigation buttons.
     * Uses Event Delegation: We listen on the parent <nav> element instead of every single button.
     */
    setupNavigation() {
        const nav = this.shadowRoot.querySelector('nav');
        
        nav.addEventListener('click', (e) => {
            const btn = e.target.closest('button');
            if (!btn) return;

            showView(btn.dataset.target);

            nav.querySelectorAll('button').forEach(b => b.classList.add('secondary'));
            
            btn.classList.remove('secondary');
        });
    }

    /**
     * Generates the HTML and internal CSS for the component.
     */
    render() {
        this.shadowRoot.innerHTML += `
            <style>
                header {
                    background: var(--bg-card);
                    border-bottom: 1px solid var(--border-color);
                    padding: 1rem 0;
                    margin-bottom: 2rem;
                }
                .container {
                    max-width: 900px; 
                    margin: 0 auto; 
                    padding: 0 20px;
                    display: flex; 
                    justify-content: space-between; 
                    align-items: center;
                }
                .brand { 
                    font-weight: 700; 
                    font-size: 1.25rem; 
                    color: var(--accent-cyan);
                    display: flex; 
                    align-items: center; 
                    gap: 8px;
                }
                .user-badge {
                    font-size: 0.8rem; 
                    border: 1px solid var(--accent-pink); 
                    color: var(--accent-pink); 
                    padding: 4px 8px; 
                    border-radius: 4px;
                }
                /* Flex container for the nav buttons */
                nav {
                    display: flex;
                    gap: 8px;
                }
            </style>

            <header>
                <div class="container">
                    
                    <div class="brand">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2M7.5 13A2.5 2.5 0 0 0 5 15.5A2.5 2.5 0 0 0 7.5 18a2.5 2.5 0 0 0 2.5-2.5A2.5 2.5 0 0 0 7.5 13m9 0a2.5 2.5 0 0 0-2.5 2.5a2.5 2.5 0 0 0 2.5 2.5a2.5 2.5 0 0 0 2.5-2.5a2.5 2.5 0 0 0-2.5-2.5"/>
                        </svg>
                        ROBOT.CTRL
                    </div>

                    <nav>
                        <button class="btn" data-target="content-view-control">Control</button>
                        <button class="btn secondary" data-target="content-view-log">Log</button>
                        <button class="btn secondary" data-target="content-view-api-test">API Tests</button>
                        <button class="btn secondary" data-target="content-view-settings">Settings</button>
                        <button class="btn secondary" data-target="content-view-about">About</button>
                    </nav>

                    <div id="user-display" class="user-badge">OP: ...</div>
                </div>
            </header>
        `;
    }
}

customElements.define('header-view', HeaderView);