import { attachSharedStyles } from '../utils/DOMUtils.js';
import { api } from '../api/RobotApiService.js';
import { store } from '../store/Store.js';

/**
 * * Renders a paginated table of the robot's action history.
 * It fetches data from the API and handles page navigation (Next/Previous).
 */
class ContentViewLog extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    /**
     * Called when the component is added to the DOM.
     */
    connectedCallback() {
        attachSharedStyles(this.shadowRoot);

        this.currentPage = 1;

        this.render();

        this.loadHistory(this.currentPage);
    }

    /**
     * Fetches action history from the API and updates the table.
     */
    async loadHistory(page) {
        const { robotId } = store.getState();
        
        const tbody = this.shadowRoot.getElementById('log-body');
        const info = this.shadowRoot.getElementById('page-info');
        
        tbody.innerHTML = '<tr><td colspan="3" class="mono">Loading data...</td></tr>';

        try {
            const data = await api.getActions(robotId, page, 5);
            
            tbody.innerHTML = '';

            if (data.actions.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" class="mono">No records found.</td></tr>';
                return;
            }

            data.actions.forEach(action => {
                const tr = document.createElement('tr');
                
                tr.innerHTML = `
                    <td class="mono" style="color:var(--text-secondary)">${action.id}</td>
                    <td style="color:var(--accent-cyan); font-weight:bold;">${action.action}</td>
                    <td class="mono">${action.detail}</td>
                `;
                
                tr.style.borderBottom = '1px solid var(--border-color)';
                
                tbody.appendChild(tr);
            });

            this.currentPage = data.page.number;
            info.innerText = `Page ${data.page.number} of ${data.page.totalPages}`;

            this.shadowRoot.getElementById('btn-prev').disabled = !data.page.hasPrevious;
            this.shadowRoot.getElementById('btn-next').disabled = !data.page.hasNext;

        } catch (e) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="3" style="color:var(--status-danger)">Error: ${e.message}</td>
                </tr>
            `;
        }
    }

    render() {
        this.shadowRoot.innerHTML += `
            <style>
                table { 
                    width: 100%; 
                    border-collapse: collapse; 
                    margin-bottom: 1rem; 
                }
                th { 
                    text-align: left; 
                    color: var(--text-secondary); 
                    font-size: 0.8rem; 
                    border-bottom: 1px solid var(--border-color); 
                    padding: 8px; 
                }
                td { 
                    padding: 12px 8px; 
                }
            </style>

            <article class="card">
                <h2>Action History</h2>
                
                <table>
                    <thead>
                        <tr>
                            <th width="10%">ID</th>
                            <th width="40%">ACTION</th>
                            <th>DETAIL</th>
                        </tr>
                    </thead>
                    <tbody id="log-body">
                        </tbody>
                </table>

                <div class="flex-row space-between">
                    <button id="btn-prev" class="btn secondary">Previous</button>
                    <span id="page-info" class="mono">Page 1</span>
                    <button id="btn-next" class="btn secondary">Next</button>
                </div>
            </article>
        `;

        this.shadowRoot.getElementById('btn-prev').onclick = () => this.loadHistory(this.currentPage - 1);
        this.shadowRoot.getElementById('btn-next').onclick = () => this.loadHistory(this.currentPage + 1);
    }
}

if(customElements.get('content-view-log')===undefined)
    customElements.define('content-view-log', ContentViewLog);