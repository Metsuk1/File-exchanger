import React, { useState } from 'react';
import { login } from '../services/api';

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await login(email, password);
            window.location.href = '/files';
        } catch (error) {
            alert('Error to login: ' + (error.response?.data || 'Check console'));
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>Log in</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit" className="btn-primary">Log in</button>
                </form>
                <p className="auth-link">
                    No account? <a href="/register">Register</a>
                </p>
            </div>
        </div>
    );
}

export default Login;