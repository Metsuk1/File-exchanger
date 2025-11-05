import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import FilesPage from './pages/FilesPage';
import Layout from './components/Layout';

function App() {
    const token = localStorage.getItem('token');

    return (
        <Router>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route
                    path="/files"
                    element={token ? <Layout><FilesPage /></Layout> : <Navigate to="/login" />}
                />
                <Route path="/" element={<Navigate to="/files" />} />
            </Routes>
        </Router>
    );
}

export default App;