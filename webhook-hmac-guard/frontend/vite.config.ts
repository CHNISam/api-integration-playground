import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
export default defineConfig(({ mode }) => { const env=loadEnv(mode,process.cwd(),''); const target=env.VITE_API_PROXY||'http://localhost:8082'; return {plugins:[vue()],server:{host:'0.0.0.0',proxy:{'/api':target,'/webhook':target}}}; });
