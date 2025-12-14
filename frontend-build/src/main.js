// Imports all components so that they end up in the bundle
import './js/components/HeaderView.js';
import './js/components/ContentViewHome.js';
import './js/components/ContentViewControl.js';
import './js/components/ContentViewLog.js';
import './js/components/ContentViewApiTest.js';
import './js/components/ContentViewSettings.js';
import './js/components/ContentViewAbout.js';

// Utility functions and API service
import { showView } from './js/utils/DOMUtils.js';
import { api } from './js/api/RobotApiService.js';

// Expose globally for easy access in the browser console
window.RobotSystem = {
    showView,
    api
};

console.log("Robot Web Components loaded successfully.");