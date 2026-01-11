import React, { useState, useRef, useEffect } from 'react';
import { chatAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

const ChatPage = () => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const messagesEndRef = useRef(null);
    const { user } = useAuth();

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        // Welcome message
        setMessages([
            {
                type: 'bot',
                text: `Hello! I'm the ${user?.companyName || 'Company'} support assistant. How can I help you today?`,
            },
        ]);
    }, [user]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        const userMessage = { type: 'user', text: input };
        setMessages((prev) => [...prev, userMessage]);
        setInput('');
        setLoading(true);

        try {
            const response = await chatAPI.sendMessage(input);
            const botMessage = {
                type: 'bot',
                text: response.data.answer,
                source: response.data.source,
                answered: response.data.answered,
            };
            setMessages((prev) => [...prev, botMessage]);
        } catch (error) {
            const errorMessage = {
                type: 'bot',
                text: 'Sorry, I encountered an error. Please try again.',
            };
            setMessages((prev) => [...prev, errorMessage]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <div className="chat-container">
                <div className="chat-header">
                    <h2>{user?.companyName} Support Chat</h2>
                    <p>Ask me anything about our services</p>
                </div>

                <div className="chat-messages">
                    {messages.map((msg, index) => (
                        <div key={index} className={`message ${msg.type}`}>
                            <div className="message-bubble">
                                {msg.text}
                                {msg.source && (
                                    <div style={{ fontSize: '12px', marginTop: '4px', opacity: 0.7 }}>
                                        Source: {msg.source}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                    {loading && (
                        <div className="message bot">
                            <div className="message-bubble">
                                <div className="spinner" style={{ width: '20px', height: '20px' }}></div>
                            </div>
                        </div>
                    )}
                    <div ref={messagesEndRef} />
                </div>

                <div className="chat-input-container">
                    <form onSubmit={handleSubmit} className="chat-input-form">
                        <input
                            type="text"
                            className="chat-input"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="Type your question..."
                            disabled={loading}
                        />
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            Send
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ChatPage;
