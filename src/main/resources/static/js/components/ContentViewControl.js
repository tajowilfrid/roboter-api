import { attachSharedStyles } from '../utils/DOMUtils.js';
import { api } from '../api/RobotApiService.js';
import { store } from '../store/Store.js';

/**
 * * The main control dashboard.
 * It handles manual robot movement via buttons or keyboard, displays live status, and shows a system log.
 */
class ContentViewControl extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }
    
    connectedCallback() {
        attachSharedStyles(this.shadowRoot);
        this.render();
        this.setupLogic();
    }

    /**
     * Returns the SVG paths for the directional arrows
     */
    get icons() {
        return {
            up:    '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M7.41 15.41L12 10.83l4.59 4.58L18 14l-6-6-6 6z"/></svg>',
            down:  '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></svg>',
            left:  '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6z"/></svg>',
            right: '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>'
        };
    }

    /**
     * Sets up all event listeners and loads the initial robot state
     */
    async setupLogic() {
        const { robotId } = store.getState();
        
        this.loadStatus(robotId);

        this.shadowRoot.querySelector('.control-pad').addEventListener('click', (e) => {
            const btn = e.target.closest('button');
            if (btn) {
                this.handleMove(btn.dataset.dir);
            }
        });

        this.unsub = store.subscribe(state => {
            const logEl = this.shadowRoot.getElementById('console');
            if (logEl) {
                logEl.innerHTML = `> ${state.lastLog}`;
            }
        });

        // Keyboard Controls
        this._keyListener = (e) => {
            const keyMap = { 
                ArrowUp: 'up', 
                ArrowDown: 'down', 
                ArrowLeft: 'left', 
                ArrowRight: 'right' 
            };

            if (keyMap[e.key] && this.isConnected) {
                e.preventDefault(); // Prevent page scrolling
                this.handleMove(keyMap[e.key]);
            }
        };
        document.addEventListener('keydown', this._keyListener);
    }

    disconnectedCallback() {
        if (this.unsub) {
            this.unsub();
        }
        document.removeEventListener('keydown', this._keyListener);
    }

    async loadStatus(id) {
        try {
            const robot = await api.getStatus(id);
            this.updateUI(robot);
            store.logSystem(`Connection to ${id} established. Energy: ${robot.energy}%`);
        } catch (e) {
            store.logSystem(`ERROR: ${e.message}`);
        }
    }

    async handleMove(direction) {
        const { robotId } = store.getState();
        try {
            const robot = await api.move(robotId, direction);
            this.updateUI(robot);
            store.logSystem(`Movement ${direction.toUpperCase()} executed.`);
        } catch (e) {
            store.logSystem(`Movement error: ${e.message}`);
        }
    }

    /**
     * Updates the HTML elements with new robot data.
     */
    updateUI(robot) {
        const root = this.shadowRoot;
        const dot = this.shadowRoot.getElementById('robot-dot');
        
        // Update text values
        root.getElementById('val-energy').innerText = `${robot.energy}%`;
        root.getElementById('val-pos').innerText = `X: ${robot.position.x} | Y: ${robot.position.y}`;
        root.getElementById('robot-header').innerText = `Unit: ${robot.id}`;
        
        // Visual indicator for low energy
        const energyBox = root.getElementById('box-energy');
        if (robot.energy < 20) {
            energyBox.style.borderLeftColor = 'var(--status-danger)';
        } else {
            energyBox.style.borderLeftColor = 'var(--status-success)';
        }

        const stepSize = 20; 
        const centerX = 100;
        const centerY = 100;
        
        // Calculate new position for the robot dot on the map
        const newLeft = centerX + (robot.position.x * stepSize);
        const newTop = centerY - (robot.position.y * stepSize);
        
        dot.style.left = `${newLeft}px`;
        dot.style.top = `${newTop}px`;
    }

    render() {
        this.shadowRoot.innerHTML += `
            <article class="card">
                
                <div class="flex-row space-between mb-1">
                    <h2 id="robot-header">Unit: --</h2>
                    <span class="mono" style="color:var(--status-success)">ONLINE</span>
                </div>

                <div class="grid-2">
                    <div class="data-box" id="box-energy" style="border-left-color: var(--status-success);">
                        <div class="data-label">System Energy</div>
                        <div class="data-value" id="val-energy">--%</div>
                    </div>
                    <div class="data-box">
                        <div class="data-label">Current Position</div>
                        <div class="data-value" id="val-pos">X: - | Y: -</div>
                    </div>
                </div>

                <div class="map-container">
                    <div id="grid-map" class="grid-map">
                        </div>
                    <div id="robot-dot" class="robot-dot">🤖</div>
                </div>

                <div class="control-pad">
                    <div></div>
                    <button class="btn btn-ctrl" data-dir="up">${this.icons.up}</button>
                    <div></div>
                    
                    <button class="btn btn-ctrl" data-dir="left">${this.icons.left}</button>
                    <button class="btn btn-ctrl" data-dir="down">${this.icons.down}</button>
                    <button class="btn btn-ctrl" data-dir="right">${this.icons.right}</button>
                </div>

                <div class="console-log" id="console">
                    > System initializing...
                </div>
            </article>
        `;
    }
}

if(customElements.get('content-view-control')===undefined)
    customElements.define('content-view-control', ContentViewControl);