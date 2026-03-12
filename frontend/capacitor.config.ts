import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.mindlog.app',
  appName: 'MindLog',
  webDir: 'build',
  server: {
    // --- DEVELOPMENT (physical device) ---
    // Uncomment to enable live reload: the WebView loads from your React
    // dev server, so the package.json proxy handles all /api calls.
    // Run `npm start` first, then `npx cap sync && npx cap run android`.
    //
    // url: 'http://192.168.68.108:3000',
    // cleartext: true,

    // --- PRODUCTION ---
    // Comment out `server.url` above, run `npm run build:mobile`,
    // then `npx cap sync && npx cap open android`.
    androidScheme: 'http',
  },
};

export default config;
