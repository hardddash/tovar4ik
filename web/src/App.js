import React from 'react';
import './App.css';
import Layout from "./Pages/Layout";
import {BrowserRouter} from "react-router-dom";
import {ChangeRouteProvider} from "routing-manager";
import {ConfirmDialogProvider} from "./Utilities/ConfirmDialog";
import {AuthProvider} from "./Utilities/Auth";

function App() {
    return (
        <div className="App">
            <RouterProvider/>
        </div>
    );
}

function RouterProvider() {
    return (
        <BrowserRouter>
            <AppProviders>
                <Layout/>
            </AppProviders>
        </BrowserRouter>
    );
}

function AppProviders({children, ...props}) {
    return (
        <ChangeRouteProvider routeMask={'/:panel'}>
            <AuthProvider>
                <ConfirmDialogProvider>
                    {children}
                </ConfirmDialogProvider>
            </AuthProvider>
        </ChangeRouteProvider>
    );
}

export default App;
