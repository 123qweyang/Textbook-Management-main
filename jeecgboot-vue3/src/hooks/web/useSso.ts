import { getToken } from '/@/utils/auth';
import { useGlobSetting } from '/@/hooks/setting';
import { validateCasLogin } from '/@/api/sys/user';
import { useUserStore } from '/@/store/modules/user';
const globSetting = useGlobSetting();
const openSso = globSetting.openSso;

function getTicketFromUrl(): string | null {
  const m = window.location.href.match(/[?&]ticket=(ST-[^&?#]+)/);
  return m ? m[1] : null;
}

function getServiceUrl(): string {
  return window.location.href.replace(/[?&]ticket=ST-[^&?#]+/, '');
}

function getHomeUrl(): string {
  return document.location.protocol + '//' + window.location.host + '/';
}

export function useSso() {

  async function ssoLogin() {
    const token = getToken();
    if (token) return;

    const ticket = getTicketFromUrl();
    if (ticket) {
      const service = getServiceUrl();
      try {
        console.log('[CAS] validating ticket:', ticket, 'service:', service);
        const res = await validateCasLogin({ ticket, service });
        console.log('[CAS] success, token:', res.token);
        const userStore = useUserStore();
        userStore.setToken(res.token);
        await userStore.afterLoginAction(true, {});
        window.history.replaceState({}, document.title, getHomeUrl());
      } catch (err) {
        console.error('[CAS] failed:', err);
      }
      return;
    }

    if (openSso == 'true') {
      window.location.href = globSetting.casBaseUrl + '/login?service=' + encodeURIComponent(getHomeUrl());
    }
  }

  async function ssoLoginOut() {
    if (openSso == 'true') {
      window.location.href = globSetting.casBaseUrl + '/logout?service=' + encodeURIComponent(getHomeUrl());
    }
  }

  return { ssoLogin, ssoLoginOut };
}
