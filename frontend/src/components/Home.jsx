import React from 'react';
import MatrixGridBackground from './MatrixGridBackground'; // ➕ Import hình nền ô lưới
import './Home.css';

export default function Home({ onStart }) {
    const algorithms = [
        { name: 'A*', class: 'sat-astar' },
        { name: 'Dijkstra', class: 'sat-dijkstra' },
        { name: 'BFS', class: 'sat-bfs' },
        { name: 'DFS', class: 'sat-dfs' }
    ];

    return (
        <div className="home-container">

            {/* 1. LAYER NỀN: Lưới ô vuông ma trận nhấp nháy */}
            <MatrixGridBackground />

            {/* 2. LAYER NỘI DUNG: Nằm đè lên trên nền nhờ class nội dung */}
            <div className="universe">

                <div className="orbit-system">
                    <div className="orbit-ellipse">
                        {algorithms.map((algo, index) => (
                            <div key={index} className={`satellite-holder ${algo.class}`}>
                                <div className="satellite-content">{algo.name}</div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="center-sun">
                    <h1 className="main-title">Pathfinding</h1>
                </div>

                <button className="explore-btn homepage-cta" onClick={onStart}>
                    Go to simulation
                    <span className="arrow">→</span>
                </button>

            </div>
        </div>
    );
}