import React, { useState, useEffect } from 'react';
import { kbAPI } from '../services/api';

const KnowledgeBasePage = () => {
    const [faqs, setFaqs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingFaq, setEditingFaq] = useState(null);
    const [formData, setFormData] = useState({ question: '', answer: '' });
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [uploading, setUploading] = useState(false);
    const [uploadMessage, setUploadMessage] = useState(null);

    useEffect(() => {
        fetchFAQs();
    }, [page]);

    const fetchFAQs = async () => {
        try {
            const response = await kbAPI.getAll(page, 10);
            setFaqs(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error('Error fetching FAQs:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingFaq) {
                await kbAPI.update(editingFaq.kbId, formData);
            } else {
                await kbAPI.create(formData);
            }
            setShowModal(false);
            setFormData({ question: '', answer: '' });
            setEditingFaq(null);
            fetchFAQs();
        } catch (error) {
            alert('Error saving FAQ');
        }
    };

    const handleEdit = (faq) => {
        setEditingFaq(faq);
        setFormData({ question: faq.question, answer: faq.answer });
        setShowModal(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this FAQ?')) {
            try {
                await kbAPI.delete(id);
                fetchFAQs();
            } catch (error) {
                alert('Error deleting FAQ');
            }
        }
    };

    const handleCsvUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploading(true);
        setUploadMessage(null);

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await kbAPI.uploadCsv(formData);
            setUploadMessage({ type: 'success', text: `Successfully uploaded ${response.data.count} FAQs!` });
            fetchFAQs();
            e.target.value = ''; // Reset file input
        } catch (error) {
            setUploadMessage({ type: 'error', text: error.response?.data?.error || 'Failed to upload CSV' });
        } finally {
            setUploading(false);
        }
    };

    if (loading) {
        return <div className="loading"><div className="spinner"></div>Loading...</div>;
    }

    return (
        <div className="container">
            <div style={{ background: 'white', borderRadius: '12px', padding: '20px', marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h2>Knowledge Base Management</h2>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <label className="btn btn-secondary" style={{ margin: 0, cursor: 'pointer' }}>
                            {uploading ? 'Uploading...' : 'Upload CSV'}
                            <input
                                type="file"
                                accept=".csv"
                                onChange={handleCsvUpload}
                                disabled={uploading}
                                style={{ display: 'none' }}
                            />
                        </label>
                        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
                            Add New FAQ
                        </button>
                    </div>
                </div>

                {uploadMessage && (
                    <div style={{
                        padding: '12px',
                        borderRadius: '8px',
                        marginBottom: '20px',
                        background: uploadMessage.type === 'success' ? '#d4edda' : '#f8d7da',
                        color: uploadMessage.type === 'success' ? '#155724' : '#721c24',
                        border: `1px solid ${uploadMessage.type === 'success' ? '#c3e6cb' : '#f5c6cb'}`
                    }}>
                        {uploadMessage.text}
                    </div>
                )}

                <div className="faq-list">
                    {faqs.length === 0 ? (
                        <p style={{ textAlign: 'center', color: 'var(--gray)' }}>
                            No FAQs yet. Add your first FAQ to get started!
                        </p>
                    ) : (
                        faqs.map((faq) => (
                            <div key={faq.kbId} className="faq-item">
                                <div className="faq-question">{faq.question}</div>
                                <div className="faq-answer">{faq.answer}</div>
                                <div className="faq-actions">
                                    <button className="btn btn-sm btn-secondary" onClick={() => handleEdit(faq)}>
                                        Edit
                                    </button>
                                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(faq.kbId)}>
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {totalPages > 1 && (
                    <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '20px' }}>
                        <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => setPage(page - 1)}
                            disabled={page === 0}
                        >
                            Previous
                        </button>
                        <span style={{ padding: '8px 16px' }}>
                            Page {page + 1} of {totalPages}
                        </span>
                        <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => setPage(page + 1)}
                            disabled={page === totalPages - 1}
                        >
                            Next
                        </button>
                    </div>
                )}
            </div>

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <h3>{editingFaq ? 'Edit FAQ' : 'Add New FAQ'}</h3>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Question</label>
                                <input
                                    type="text"
                                    value={formData.question}
                                    onChange={(e) => setFormData({ ...formData, question: e.target.value })}
                                    required
                                    placeholder="Enter the question"
                                />
                            </div>
                            <div className="form-group">
                                <label>Answer</label>
                                <textarea
                                    value={formData.answer}
                                    onChange={(e) => setFormData({ ...formData, answer: e.target.value })}
                                    required
                                    rows="5"
                                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '2px solid var(--border)' }}
                                    placeholder="Enter the answer"
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => {
                                    setShowModal(false);
                                    setEditingFaq(null);
                                    setFormData({ question: '', answer: '' });
                                }}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary">
                                    {editingFaq ? 'Update' : 'Create'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default KnowledgeBasePage;
