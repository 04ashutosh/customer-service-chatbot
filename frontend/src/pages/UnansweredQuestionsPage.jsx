import React, { useState, useEffect } from 'react';
import { unansweredAPI } from '../services/api';

const UnansweredQuestionsPage = () => {
    const [questions, setQuestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [selectedQuestion, setSelectedQuestion] = useState(null);
    const [answer, setAnswer] = useState('');

    useEffect(() => {
        fetchQuestions();
    }, []);

    const fetchQuestions = async () => {
        try {
            const response = await unansweredAPI.getAll('NEW', 0, 50);
            setQuestions(response.data.content);
        } catch (error) {
            console.error('Error fetching questions:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = (question) => {
        setSelectedQuestion(question);
        setAnswer('');
        setShowModal(true);
    };

    const handleSubmitApproval = async (e) => {
        e.preventDefault();
        try {
            await unansweredAPI.approve(selectedQuestion.questionId, answer);
            setShowModal(false);
            setSelectedQuestion(null);
            setAnswer('');
            fetchQuestions();
        } catch (error) {
            alert('Error approving question');
        }
    };

    const handleReject = async (id) => {
        if (window.confirm('Are you sure you want to reject this question?')) {
            try {
                await unansweredAPI.reject(id);
                fetchQuestions();
            } catch (error) {
                alert('Error rejecting question');
            }
        }
    };

    if (loading) {
        return <div className="loading"><div className="spinner"></div>Loading...</div>;
    }

    return (
        <div className="container">
            <div style={{ background: 'white', borderRadius: '12px', padding: '20px' }}>
                <h2 style={{ marginBottom: '20px' }}>Unanswered Questions</h2>

                {questions.length === 0 ? (
                    <p style={{ textAlign: 'center', color: 'var(--gray)', padding: '40px' }}>
                        No unanswered questions at the moment. Great job! 🎉
                    </p>
                ) : (
                    <div className="faq-list">
                        {questions.map((q) => (
                            <div key={q.questionId} className="faq-item">
                                <div className="faq-question">{q.question}</div>
                                <div style={{ fontSize: '14px', color: 'var(--gray)', marginBottom: '12px' }}>
                                    Asked {q.frequency} time{q.frequency > 1 ? 's' : ''}
                                </div>
                                <div className="faq-actions">
                                    <button className="btn btn-sm btn-success" onClick={() => handleApprove(q)}>
                                        Approve & Add to KB
                                    </button>
                                    <button className="btn btn-sm btn-danger" onClick={() => handleReject(q.questionId)}>
                                        Reject
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <h3>Add Answer to Knowledge Base</h3>
                        <p style={{ color: 'var(--gray)', marginBottom: '20px' }}>
                            <strong>Question:</strong> {selectedQuestion?.question}
                        </p>
                        <form onSubmit={handleSubmitApproval}>
                            <div className="form-group">
                                <label>Answer</label>
                                <textarea
                                    value={answer}
                                    onChange={(e) => setAnswer(e.target.value)}
                                    required
                                    rows="5"
                                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '2px solid var(--border)' }}
                                    placeholder="Enter the answer for this question"
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-success">
                                    Approve & Add
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UnansweredQuestionsPage;
