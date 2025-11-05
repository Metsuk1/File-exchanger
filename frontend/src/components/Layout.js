import { Link, useNavigate } from 'react-router-dom';

function Layout({ children }) {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    return (
        <div className="layout">
            <header className="header">
                <h1>Cloud Storage</h1>
                <button onClick={handleLogout} className="btn-logout">Quit</button>
            </header>
            <main className="main-content">
                {children}
            </main>
        </div>
    );
}

export default Layout;