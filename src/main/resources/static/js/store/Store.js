/**
 * * This class implements a simple "Publish/Subscribe" pattern to manage the global application state.
 */
class Store {

    constructor() {
        this.state = {
            username: "Commander",
            robotId: "r1", // The currently selected robot ID
            lastLog: "System initialized. Waiting for input..."
        };

        this.listeners = new Set();
    }

    getState() {
        return { ...this.state };
    }

    subscribe(callback) {
        this.listeners.add(callback);

        callback(this.getState());
        
        return () => this.listeners.delete(callback);
    }

    /**
     * Internal helper to let all listeners know that something changed.
     */
    _notify() {
        const snapshot = this.getState();
        this.listeners.forEach(callback => callback(snapshot));
    }

    /**
     * Actions (Methods to modify the state)
     */
    setUsername(name) {
        this.state.username = name;
        this._notify();
    }

    setRobotId(id) {
        this.state.robotId = id;
        this.logSystem(`Target system changed to: ${id}`);
        this._notify();
    }

    logSystem(message) {
        const time = new Date().toLocaleTimeString();
        this.state.lastLog = `[${time}] ${message}`;
        this._notify();
    }
}

// Export a single instance so the whole app shares the same state
export const store = new Store();