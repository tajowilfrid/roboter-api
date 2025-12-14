/**
 * RobotApiService
 * * This class handles all communication with the Spring Boot Backend.
 * It encapsulates the 'fetch' calls so we don't have to repeat code in our components.
 */
class RobotApiService {

    constructor() {
        // The base URL for all robot-related endpoints
        this.baseUrl = '/robots';
    }

    async _request(path, options = {}) {
        const url = `${this.baseUrl}${path}`;
        
        const defaultHeaders = { 'Content-Type': 'application/json' };

        const config = {
            ...options,
            headers: { ...defaultHeaders, ...options.headers }
        };

        try {
            const response = await fetch(url, config);

            if (response.status === 204) {
                return null;
            }

            // Check if the request was successful (Status 200-299)
            if (!response.ok) {
                const errorText = await response.text().catch(() => 'Unknown Error');
                throw new Error(`${response.status} - ${errorText}`);
            }
            return await response.json();

        } catch (error) {
            console.error(`API Error at ${path}:`, error);
            throw error;
        }
    }

    /**
     * Public Methods (used by the Components)
     */

    getStatus(id) {
        return this._request(`/${encodeURIComponent(id)}/status`);
    }

    move(id, direction) {
        return this._request(`/${encodeURIComponent(id)}/move`, {
            method: 'POST',
            body: JSON.stringify({ direction })
        });
    }

    updateState(id, patchData) {
        return this._request(`/${encodeURIComponent(id)}/state`, {
            method: 'PATCH',
            body: JSON.stringify(patchData)
        });
    }

    attack(attackerId, targetId) {
        const attacker = encodeURIComponent(attackerId);
        const target = encodeURIComponent(targetId);
        
        return this._request(`/${attacker}/attack/${target}`, {
            method: 'POST'
        });
    }

    getActions(id, page = 1, size = 5) {
        const safeId = encodeURIComponent(id);
        return this._request(`/${safeId}/actions?page=${page}&size=${size}`);
    }
}

// Export a single instance (Singleton pattern) to be used throughout the app
export const api = new RobotApiService();