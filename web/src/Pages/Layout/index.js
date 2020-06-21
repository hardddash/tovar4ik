import React from 'react';
import clsx from 'clsx';
import Drawer from '@material-ui/core/Drawer';
import CssBaseline from '@material-ui/core/CssBaseline';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import InboxIcon from '@material-ui/icons/MoveToInbox';
import {useStyles} from "./styles";
import Route from "react-router-dom/es/Route";
import Auth from "../Auth";
import {Switch} from 'react-router-dom'
import Goods from "../Goods";
import {useChangeRoute} from "routing-manager";
import {useAuth} from "../../Utilities/Auth";
import Groups from "../Groups";
import ExitToAppIcon from '@material-ui/icons/ExitToApp';
import Grid from "@material-ui/core/Grid";


function PagesSwitch() {
    return (
        <Switch>
            <Route path={"/goods"}>
                <Goods/>
            </Route>
            <Route path={"/groups"}>
                <Groups />
            </Route>
            <Route path={"/login"}>
                <Auth/>
            </Route>
        </Switch>
    );
}

function PanelButton({label, onClick, icon, ...props}) {
    return (
        <ListItem button onClick={onClick} {...props}>
            {icon && <ListItemIcon>{icon}</ListItemIcon>}
            <ListItemText primary={label}/>
        </ListItem>
    );
}


export default function Layout() {
    const classes = useStyles();
    const [drawerOpened, setDrawerOpened] = React.useState(true);
    const {changeRoute} = useChangeRoute();
    const {user, token, setToken} = useAuth();
    if (!token) {
        return (
            <Grid container justify={"center"} style={{height: '100vh'}}>
                <div className={classes.loginContainer}>
                    <div>
                        <Auth label={<Typography variant={'h5'}> Login </Typography>}/>
                    </div>
                </div>
            </Grid>
        );
    }

    return (
        <div className={classes.root}>
            <CssBaseline/>
            <AppBar
                position="fixed"
                className={clsx(classes.appBar, {
                    [classes.appBarShift]: drawerOpened,
                })}
            >
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={() => setDrawerOpened(true)}
                        edge="start"
                        className={clsx(classes.menuButton, drawerOpened && classes.hide)}
                    >
                        <MenuIcon/>
                    </IconButton>
                    <Typography variant="h6" noWrap>
                        Persistent drawer
                    </Typography>
                    <IconButton
                        className={clsx(classes.signOutButton)}
                        color={"inherit"}
                        onClick={() => setToken(null)}
                    >
                        <ExitToAppIcon />
                    </IconButton>
                </Toolbar>
            </AppBar>
            <Drawer
                className={classes.drawer}
                variant="persistent"
                anchor="left"
                open={drawerOpened}
                classes={{
                    paper: classes.drawerPaper,
                }}
            >
                <div className={classes.drawerHeader}>
                    <IconButton onClick={() => setDrawerOpened(false)}>
                        <ChevronLeftIcon/>
                    </IconButton>
                </div>
                <Divider/>
                <List>
                    <PanelButton label={'Goods'} icon={<InboxIcon/>} onClick={event => changeRoute({panel: 'goods'})}/>
                    <PanelButton label={'Groups'} icon={<InboxIcon/>} onClick={event => changeRoute({panel: 'groups'})}/>
                </List>
            </Drawer>
            <main
                className={clsx(classes.content, {
                    [classes.contentShift]: drawerOpened,
                })}
            >
                <div className={classes.drawerHeader}/>
                <PagesSwitch/>
            </main>
        </div>
    );
}
