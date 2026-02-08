import { useState, useEffect } from 'react';
import { getProfile, updateProfile } from '../services/api';

function Profile() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [editing, setEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        getProfile()
            .then(res => {
                setName(res.data.name);
                setEmail(res.data.email);
                setLoading(false);
            })
            .catch(() => {
                setError('Failed to load profile');
                setLoading(false);
            });
    }, []);

    const handleSave = async () => {
        setError('');
        setSuccess('');
        try {
            const res = await updateProfile(name, email);
            setName(res.data.name);
            setEmail(res.data.email);
            localStorage.setItem('userEmail', res.data.email);
            setSuccess('Profile updated successfully');
            setEditing(false);
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to update profile');
        }
    };

    const handleCancel = () => {
        setEditing(false);
        setError('');
        setSuccess('');
        getProfile().then(res => {
            setName(res.data.name);
            setEmail(res.data.email);
        });
    };

    if (loading) return <div className="profile-container"><p>Loading...</p></div>;

    return (
        <div className="profile-container">
            <div className="profile-card">
                <h2>Profile</h2>

                {error && <div className="error">{error}</div>}
                {success && <div className="success">{success}</div>}

                <div className="profile-field">
                    <label>Name</label>
                    {editing ? (
                        <input
                            value={name}
                            onChange={e => setName(e.target.value)}
                        />
                    ) : (
                        <p>{name}</p>
                    )}
                </div>

                <div className="profile-field">
                    <label>Email</label>
                    {editing ? (
                        <input
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                        />
                    ) : (
                        <p>{email}</p>
                    )}
                </div>

                <div className="profile-actions">
                    {editing ? (
                        <>
                            <button className="btn-primary" onClick={handleSave}>Save</button>
                            <button className="btn-secondary" onClick={handleCancel}>Cancel</button>
                        </>
                    ) : (
                        <button className="btn-primary" onClick={() => setEditing(true)}>Edit</button>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Profile;
