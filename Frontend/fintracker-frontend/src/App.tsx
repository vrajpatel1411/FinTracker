import CustomRoutes from "./routes/AppRoutes";
import ErrorBoundary from "./ErrorBoundary";

function App() {
  return (
    <ErrorBoundary>
      <CustomRoutes />
    </ErrorBoundary>
  );
}

export default App;
