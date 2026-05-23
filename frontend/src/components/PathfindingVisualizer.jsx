import React, { useState, useEffect, useRef } from 'react';
import './PathfindingVisualizer.css';
import MatrixGridBackground from './MatrixGridBackground';

const WIDTH = 40;
const HEIGHT = 17;

// ✅ SỬA LỖI 1: Thêm giao thức https:// vào trước URL server Railway của bạn
const BACKEND_URL = 'https://path-finding-simulation-production.up.railway.app';

export default function PathfindingVisualizer() {
    const [algorithm, setAlgorithm] = useState('ASTAR');
    const [allowDiagonal, setAllowDiagonal] = useState('false');
    const [walls, setWalls] = useState(new Set());
    const [isMouseDown, setIsMouseDown] = useState(false);
    const [history, setHistory] = useState([]);

    // --- Quản lý tọa độ động của điểm xuất phát và đích ---
    const [startNode, setStartNode] = useState({ x: 1, y: 7 });

    // ✅ SỬA LỖI 2: Đưa tọa độ x về 38 để nằm gọn trong bảng 40 ô (từ 0 đến 39)
    const [endNode, setEndNode] = useState({ x: 38, y: 7 });

    // --- Đang kéo vật thể nào trên lưới ---
    const [draggedNodeType, setDraggedNodeType] = useState('NONE');

    const eventSourceRef = useRef(null);

    const clearGridVisuals = () => {
        if (eventSourceRef.current) eventSourceRef.current.close();
        for (let y = 0; y < HEIGHT; y++) {
            for (let x = 0; x < WIDTH; x++) {
                const cell = document.getElementById(`cell-${x}-${y}`);
                if (cell) cell.classList.remove('explored', 'path');
            }
        }
    };

    const handleReset = () => {
        clearGridVisuals();
        setWalls(new Set());
        setStartNode({ x: 1, y: 7 });
        setEndNode({ x: 38, y: 7 }); // Đồng bộ reset về ô số 38
        setDraggedNodeType('NONE');
    };

    const handleMouseDown = (x, y) => {
        clearGridVisuals();
        setIsMouseDown(true);

        if (x === startNode.x && y === startNode.y) {
            setDraggedNodeType('START');
        } else if (x === endNode.x && y === endNode.y) {
            setDraggedNodeType('END');
        } else {
            setDraggedNodeType('NONE');
            const newWalls = new Set(walls);
            const key = `${x},${y}`;
            if (newWalls.has(key)) newWalls.delete(key);
            else newWalls.add(key);
            setWalls(newWalls);
        }
    };

    const handleMouseEnter = (x, y) => {
        if (!isMouseDown) return;

        const key = `${x},${y}`;
        if (draggedNodeType === 'START') {
            if ((x === endNode.x && y === endNode.y) || walls.has(key)) return;
            setStartNode({ x, y });
        } else if (draggedNodeType === 'END') {
            if ((x === startNode.x && y === startNode.y) || walls.has(key)) return;
            setEndNode({ x, y });
        } else {
            if ((x === startNode.x && y === startNode.y) || (x === endNode.x && y === endNode.y)) return;
            if (!walls.has(key)) {
                const newWalls = new Set(walls);
                newWalls.add(key);
                setWalls(newWalls);
            }
        }
    };

    useEffect(() => {
        const handleMouseUp = () => {
            setIsMouseDown(false);
            setDraggedNodeType('NONE');
        };
        window.addEventListener('mouseup', handleMouseUp);
        return () => {
            window.removeEventListener('mouseup', handleMouseUp);
            if (eventSourceRef.current) eventSourceRef.current.close();
        };
    }, [isMouseDown, draggedNodeType]);

    const generateMaze = () => {
        clearGridVisuals();

        const url = `${BACKEND_URL}/api/pathfinding/generate-maze?width=${WIDTH}&height=${HEIGHT}`;
        eventSourceRef.current = new EventSource(url);

        let tempWalls = new Set();

        eventSourceRef.current.addEventListener('maze-step', (event) => {
            const data = JSON.parse(event.data);

            if (data.type === 'WALL') {
                if ((data.x === startNode.x && data.y === startNode.y) || (data.x === endNode.x && data.y === endNode.y)) return;
                tempWalls.add(`${data.x},${data.y}`);
                setWalls(new Set(tempWalls));
            }
            else if (data.type === 'EMPTY') {
                tempWalls.delete(`${data.x},${data.y}`);
                setWalls(new Set(tempWalls));
            }
            else if (data.type === 'DONE') {
                console.log('Sinh mê cung hoàn tất.');
                eventSourceRef.current.close();
            }
        });

        eventSourceRef.current.onerror = (err) => {
            console.error('Lỗi Stream khi sinh mê cung:', err);
            eventSourceRef.current.close();
        };
    };

    // ✅ SỬA LỖI 3: Tích hợp hoàn chỉnh cơ chế đếm Real-time Stream và cập nhật Hàng đợi Queue lịch sử
    const startSimulation = () => {
        clearGridVisuals();

        const startTime = performance.now();
        let exploredCount = 0;

        const wallsStr = Array.from(walls).join('-');
        const currentAlgorithm = algorithm === 'ASTAR' ? 'A* Search' : algorithm;

        const url = `${BACKEND_URL}/api/pathfinding/solve-realtime`
            + `?algorithm=${algorithm}`
            + `&startX=${startNode.x}`
            + `&startY=${startNode.y}`
            + `&endX=${endNode.x}`
            + `&endY=${endNode.y}`
            + `&width=${WIDTH}`
            + `&height=${HEIGHT}`
            + `&allowDiagonal=${allowDiagonal}`
            + `&wallsStr=${wallsStr}`;

        eventSourceRef.current = new EventSource(url);

        const handleAlgorithmStep = (event) => {
            const data = JSON.parse(event.data);
            const cell = document.getElementById(`cell-${data.x}-${data.y}`);

            if (!cell || (data.x === startNode.x && data.y === startNode.y) || (data.x === endNode.x && data.y === endNode.y)) return;

            if (data.type === 'EXPLORED') {
                cell.classList.add('explored');
                exploredCount++;
            } else if (data.type === 'PATH') {
                cell.classList.remove('explored');
                cell.classList.add('path');
            } else if (data.type === 'DONE') {
                const endTime = performance.now();
                const executionTime = (endTime - startTime).toFixed(2);

                setHistory(prevHistory => {
                    const newEntry = {
                        id: Date.now(),
                        algorithmName: currentAlgorithm,
                        exploredCount: exploredCount,
                        duration: executionTime
                    };

                    const updatedHistory = [...prevHistory, newEntry];
                    if (updatedHistory.length > 5) {
                        updatedHistory.shift();
                    }
                    return updatedHistory;
                });

                if (eventSourceRef.current) {
                    eventSourceRef.current.removeEventListener('algorithm-step', handleAlgorithmStep);
                    eventSourceRef.current.close();
                }
            }
        };

        eventSourceRef.current.addEventListener('algorithm-step', handleAlgorithmStep);

        eventSourceRef.current.onerror = (err) => {
            console.error(err);
            if (eventSourceRef.current) eventSourceRef.current.close();
        };
    };

    const renderGrid = () => {
        const gridRows = [];
        for (let y = 0; y < HEIGHT; y++) {
            for (let x = 0; x < WIDTH; x++) {
                let cellClass = 'cell';

                if (x === startNode.x && y === startNode.y) cellClass += ' start';
                else if (x === endNode.x && y === endNode.y) cellClass += ' end';
                else if (walls.has(`${x},${y}`)) cellClass += ' wall';

                gridRows.push(
                    <div
                        key={`${x}-${y}`}
                        id={`cell-${x}-${y}`}
                        className={cellClass}
                        onMouseDown={() => handleMouseDown(x, y)}
                        onMouseEnter={() => handleMouseEnter(x, y)}
                        onDragStart={(e) => e.preventDefault()}
                    />
                );
            }
        }
        return gridRows;
    };

    return (
        <div className="visualizer-container">
            <MatrixGridBackground />
            <h2>Path Finding Simulation</h2>
            <p style={{ color: '#64748b', fontSize: '13px', marginTop: '-15px', marginBottom: '15px' }}>
                * Start point and end point can be dragged!
            </p>

            <div className="controls-bar">
                <div className="control-group">
                    <label>Algorithm: </label>
                    <select value={algorithm} onChange={(e) => setAlgorithm(e.target.value)}>
                        <option value="ASTAR">A* Search</option>
                        <option value="DIJKSTRA">Dijkstra</option>
                        <option value="BFS">Breadth-First Search (BFS)</option>
                        <option value="DFS">Depth-First Search (DFS)</option>
                    </select>
                </div>

                <div className="control-group">
                    <label>Diagonal movement: </label>
                    <select value={allowDiagonal} onChange={(e) => setAllowDiagonal(e.target.value)}>
                        <option value="false">No</option>
                        <option value="true">Yes</option>
                    </select>
                </div>

                <button className="btn-start" onClick={startSimulation}>Simulate</button>
                <button className="btn-maze" onClick={generateMaze} style={{ backgroundColor: '#8b5cf6', color: 'white', fontWeight: 600 }}>
                    Maze generation
                </button>
                <button className="btn-clear" onClick={handleReset}>Clear map</button>
            </div>

            <div className="legend-bar">
                <div className="legend-node"><div className="node-box start"></div> Start</div>
                <div className="node-node"><div className="node-box end"></div> End</div>
                <div className="node-node"><div className="node-box wall"></div> Wall</div>
                <div className="node-node"><div className="node-box explored"></div> Explored</div>
                <div className="node-node"><div className="node-box path"></div> Shortest path</div>
            </div>

            <div
                className="grid-board"
                style={{ gridTemplateColumns: `repeat(${WIDTH}, 30px)` }}
            >
                {renderGrid()}
            </div>

            {/* ✅ KHU VỰC HIỂN THỊ LỊCH SỬ ĐÃ ĐƯỢC KẾT NỐI DATA STATE */}
            {/* <div className="history-panel">
                <h3 className="history-title">⏱️ Execution History (Maximum 5 recent runs)</h3>
                {history.length === 0 ? (
                    <p className="history-empty">No simulation data yet. Click "Simulate" to start!</p>
                ) : (
                    <div className="history-row-container">
                        {history.map((item, index) => (
                            <div key={item.id} className="history-card">
                                <span className="history-badge">#{index + 1}</span>
                                <div className="history-info">
                                    <span className="info-label">Algorithm:</span>
                                    <span className="info-value algo-name">{item.algorithmName}</span>
                                </div>
                                <div className="history-info">
                                    <span className="info-label">Explored Nodes:</span>
                                    <span className="info-value explored-val">{item.exploredCount}</span>
                                </div>
                                <div className="history-info">
                                    <span className="info-label">Duration:</span>
                                    <span className="info-value time-val">{item.duration} ms</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div> */}

        </div>
    );
}