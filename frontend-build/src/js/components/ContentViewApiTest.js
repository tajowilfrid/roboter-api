import { attachSharedStyles } from '../utils/DOMUtils.js';
import { api } from '../api/RobotApiService.js';
import { store } from '../store/Store.js';

/**
 * * Renders a dashboard to manually trigger API endpoints.
 * This effectively replaces the need for an external tool like Postman for basic checks.
 * It uses the definitions from 'api-tests.http'.
 */
class ContentViewApiTest extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    connectedCallback() {
        // Inject global styles
        attachSharedStyles(this.shadowRoot);

        this.render();
        this.setupTests();
    }

    /**
     * Defines the available tests and renders them into the list
     */
    setupTests() {
        const { robotId } = store.getState();
        
        const container = this.shadowRoot.getElementById('test-list');

        // Define the test cases (matching api-tests.http)
        const tests = [
            {
                name: "1. Get Status (HATEOAS)",
                method: "GET",
                path: `/robots/${robotId}/status`,
                action: () => api.getStatus(robotId)
            },
            {
                name: "2. Move Robot (UP)",
                method: "POST",
                path: `/robots/${robotId}/move`,
                action: () => api.move(robotId, "up")
            },
            {
                name: "3. Move Robot (RIGHT)",
                method: "POST",
                path: `/robots/${robotId}/move`,
                action: () => api.move(robotId, "right")
            },
            {
                name: "4. Pick up Item (Diamond)",
                method: "POST",
                path: `/robots/${robotId}/pickup/diamant`,

                action: async () => {
                    const res = await fetch(`/robots/${robotId}/pickup/diamant`, { method: 'POST' });
                    return res.json();
                }
            },
            {
                name: "5. Patch State (Energy -> 80)",
                method: "PATCH",
                path: `/robots/${robotId}/state`,
                action: () => api.updateState(robotId, { energy: 80 })
            },
            {
                name: "6. Pagination (Page 1, Size 2)",
                method: "GET",
                path: `/robots/${robotId}/actions?page=1&size=2`,
                action: () => api.getActions(robotId, 1, 2)
            }
        ];

        // Loop through the definitions and create DOM elements
        tests.forEach(test => {
            this.createTestItem(test, container);
        });
    }

    /**
     * Helper method to create a single test row in the UI
     */
    createTestItem(test, container) {
        const wrapper = document.createElement('div');
        wrapper.style.marginBottom = '1rem';

        const headerRow = document.createElement('div');
        headerRow.className = 'endpoint-item';
        headerRow.innerHTML = `
            <div>
                <span class="method ${test.method}">${test.method}</span>
                <span class="mono">${test.path}</span>
                <div style="font-size:0.8rem; color:var(--text-secondary); margin-top:4px;">
                    ${test.name}
                </div>
            </div>
            <button class="btn secondary">RUN</button>
        `;

        const resultBox = document.createElement('div');
        resultBox.className = 'console-log';
        resultBox.style.display = 'none';
        resultBox.style.height = 'auto';
        resultBox.style.maxHeight = '200px';
        resultBox.style.marginTop = '10px';

        const runBtn = headerRow.querySelector('button');
        runBtn.onclick = async () => {
            resultBox.style.display = 'block';
            resultBox.innerText = 'Sending Request...';
            resultBox.style.color = 'var(--text-secondary)';

            try {
                const json = await test.action();
                
                resultBox.innerText = JSON.stringify(json, null, 2);
                resultBox.style.color = 'var(--status-success)';
            } catch (e) {
                resultBox.innerText = `Error: ${e.message}`;
                resultBox.style.color = 'var(--status-danger)';
            }
        };

        wrapper.appendChild(headerRow);
        wrapper.appendChild(resultBox);
        container.appendChild(wrapper);
    }

    render() {
        this.shadowRoot.innerHTML += `
            <article class="card">
                <h2>API Endpoint Tests</h2>
                <p>
                    Live Dashboard based on <code>api-tests.http</code>.
                    <br>
                    Use this to verify the backend logic without leaving the browser.
                </p>
                
                <div id="test-list" class="mt-1"></div>
            </article>
        `;
    }
}

if(customElements.get('content-view-api-test')===undefined)
    customElements.define('content-view-api-test', ContentViewApiTest);