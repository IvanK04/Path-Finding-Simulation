import React, { useState } from 'react';
import Home from './components/Home';
import PathfindingVisualizer from './components/PathfindingVisualizer';
import './index.css'; // Sẽ thêm hiệu ứng tổng ở đây

function App() {
  // 'HOME', 'TRANSITION', 'VISUALIZER'
  const [screen, setScreen] = useState('HOME');

  const handleStartTransition = () => {
    setScreen('TRANSITION');

    // SỬA TẠI ĐÂY: Chờ đúng 800ms (bằng thời gian hiệu ứng CSS fade-out)
    setTimeout(() => {
      setScreen('VISUALIZER');
    }, 800);
  };

  return (
    <div className="App">
      {/* Nếu đang ở HOME hoặc trong quá trình TRANSITION thì vẫn vẽ trang chủ */}
      {(screen === 'HOME' || screen === 'TRANSITION') && (
        <div className={screen === 'TRANSITION' ? 'page-exit-active' : ''}>
          <Home onStart={handleStartTransition} />
        </div>
      )}

      {/* Nếu đã bước vào TRANSITION hoặc VISUALIZER thì nạp sẵn trang mô phỏng */}
      {(screen === 'VISUALIZER' || screen === 'TRANSITION') && (
        <div className={screen === 'TRANSITION' ? 'page-enter-prepare' : 'page-enter-active'}>
          <PathfindingVisualizer />
        </div>
      )}
    </div>
  );
}

export default App;