import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
const Footer: React.FC = () => {
  const defaultMessage = '用户中心';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      copyright={`${currentYear} ${defaultMessage}`}
      links={[
        {
          key: 'github',
          title: <><GithubOutlined /> GitHub</>,
          href: 'https://github.com/ChenYouXin8/user-center',
          blankTarget: true,
        },
      ]}
    />
  );
};
export default Footer;
