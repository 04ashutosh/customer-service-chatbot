import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const DashboardLayout = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="dashboard">
            <div className="dashboard-header">
                <div>
                    <h1>{user?.companyName}</h1>
                    <p style={{ color: 'var(--gray)', fontSize: '14px', marginTop: '4px' }}>
                        {user?.email} • {user?.role}
                    </p>
                </div>
                <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                    <nav className="nav-links">
                        <Link to="/dashboard/chat" className="nav-link">Chat</Link>
                        {user?.role === 'ADMIN' && (
                            <>
                                <Link to="/dashboard/knowledge-base" className="nav-link">Knowledge Base</Link>
                                <Link to="/dashboard/unanswered" className="nav-link">Unanswered</Link>
                            </>
                        )}
                    </nav>
                    <button className="btn btn-danger btn-sm" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </div>
            <Outlet />
        </div>
    );
};

export default DashboardLayout;
