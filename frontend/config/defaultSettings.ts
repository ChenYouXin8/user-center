import { Settings as LayoutSettings } from '@ant-design/pro-components';

const Settings: LayoutSettings & {
  pwa?: boolean;
  logo?: string;
} = {
  navTheme: 'light',
  // 拂晓蓝
  primaryColor: '#1890ff',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: false,
  fixSiderbar: true,
  colorWeak: false,
  title: '用户中心',
  pwa: false,
  logo: 'https://pic.code-nav.cn/user_avatar/1662025280472055809/8j67wrRT-logo.jpg',
  iconfontUrl: '',
};

export default Settings;
