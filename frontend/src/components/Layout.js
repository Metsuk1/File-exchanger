import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';

function Layout({ children }) {
    const navigate = useNavigate();
    const [darkMode, setDarkMode] = useState(false);

    useEffect(() => {
        const isDark = localStorage.getItem('darkMode') === 'true' ||
            (!localStorage.getItem('darkMode') && window.matchMedia('(prefers-color-scheme: dark)').matches);
        setDarkMode(isDark);
        document.body.classList.toggle('dark', isDark);
    }, []);

    const toggleTheme = () => {
        const newDark = !darkMode;
        setDarkMode(newDark);
        document.body.classList.toggle('dark', newDark);
        localStorage.setItem('darkMode', newDark);
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    return (
        <>
            <nav className="navbar">
                <div className="navbar-brand">
                    <i className="fas fa-cloud-upload-alt"></i>
                    <span>Cloudex</span>
                </div>
                <div className="navbar-actions">
                    <button onClick={toggleTheme} className="theme-toggle">
                        {darkMode ? <i className="fas fa-sun"></i> : <i className="fas fa-moon"></i>}
                    </button>
                    <div className="user-avatar">
                        {localStorage.getItem('userEmail')?.[0].toUpperCase() || 'U'}
                    </div>
                    <button onClick={handleLogout} className="btn-logout">
                        Quit
                    </button>
                </div>
            </nav>
            <div className="main-content">
                {children}
            </div>
        </>
    );
}

export default Layout;