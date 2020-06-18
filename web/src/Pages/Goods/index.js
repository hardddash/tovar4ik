import React from "react";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import TableContainer from "@material-ui/core/TableContainer";
import Table from "@material-ui/core/Table";
import TableRow from "@material-ui/core/TableRow";
import TableHead from "@material-ui/core/TableHead";
import TableCell from "@material-ui/core/TableCell";
import {useStyles} from "./styles";
import TableBody from '@material-ui/core/TableBody';
import IconButton from "@material-ui/core/IconButton";
import AddIcon from '@material-ui/icons/Add';
import EditIcon from '@material-ui/icons/Edit';
import InputBase from "@material-ui/core/InputBase";
import Divider from "@material-ui/core/Divider";
import MenuIcon from '@material-ui/icons/Menu';
import SearchIcon from '@material-ui/icons/Search';
import DirectionsIcon from '@material-ui/icons/Directions';
import Box from "@material-ui/core/Box";
import clsx from "clsx";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import ButtonGroup from "@material-ui/core/ButtonGroup";
import {coreRequest} from "../../Utilities/Rest";


function DataDialogEditor({onClose, onFinish, open, idata}) {
    const [data, setData] = React.useState(idata || {});

    const header = idata ? `Good: ${data.name}` : 'Good: New good'

    React.useEffect(() => {
        setData(idata || {});
    }, [idata])

    function handleAdd() {
        coreRequest().post('goods')
            .send(data)
            .then(response => {
                onClose && onClose();
            })
            .catch(console.error);
        onFinish && onFinish();
    }

    function handleEdit() {
        coreRequest().put('good')
            .send(data)
            .query({id: +idata.id})
            .then(response => {
                onClose && onClose();
            })
            .catch(console.error);
        onFinish && onFinish();
    }

    function handleInput(event) {
        event.persist();
        setData(last => ({...last, [event.target.name]: event.target.value}));
    }

    return (
        <Dialog onClose={onClose} aria-labelledby="simple-dialog-title" open={open}>
            <DialogTitle id="simple-dialog-title">{header}</DialogTitle>
            <List>
                <ListItem>
                    <TextField
                        fullWidth
                        placeholder={'Name'}
                        name={'name'}
                        value={data.name}
                        onChange={handleInput}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        placeholder={'Description'}
                        name={'description'}
                        value={data.description}
                        onChange={handleInput}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        placeholder={'Producer'}
                        name={'producer'}
                        value={data.producer}
                        onChange={handleInput}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        placeholder={'Quantity'}
                        name={'quantity'}
                        value={data.quantity}
                        onChange={handleInput}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        placeholder={'Price'}
                        name={'price'}
                        value={data.price}
                        onChange={handleInput}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        placeholder={'Group'}
                        name={'group_id'}
                        value={data.group_id}
                        onChange={handleInput}
                    />
                </ListItem>
            </List>
            <ListItem>
                <ButtonGroup fullWidth variant={'text'}>
                    <Button onClick={idata ? handleEdit : handleAdd}>
                        {idata ? 'Edit' : 'Add'}
                    </Button>
                    <Button onClick={onClose}>
                        Cancel
                    </Button>
                </ButtonGroup>
            </ListItem>
        </Dialog>
    );
}

export default function Goods() {
    const [goods, setGoods] = React.useState([]);
    const [rowId, setRowId] = React.useState(0);
    const [dataDialogOpen, setDataDialogOpen] = React.useState(false);
    const classes = useStyles();

    function handleUpdate() {
        coreRequest().get(`goods`)
            .then(response => {
                setGoods(response.body);
            })
            .catch(console.error);
    }

    React.useEffect(() => {
        handleUpdate();
    }, []);

    return (
        <React.Fragment>
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Box p={1}>
                            <div style={{display: 'flex', justifyContent: 'flex-end'}}>
                                <div style={{display: 'inline-block'}}>
                                    <InputBase
                                        className={classes.input}
                                        placeholder="Search good"
                                        inputProps={{'aria-label': 'search google maps'}}
                                    />
                                    <IconButton type="submit" className={classes.iconButton} aria-label="search">
                                        <SearchIcon/>
                                    </IconButton>
                                </div>
                            </div>

                            <TableContainer>
                                <Table className={classes.table} aria-label="simple table" size={"small"}>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Name</TableCell>
                                            <TableCell align="right">Description</TableCell>
                                            <TableCell align="right">Price</TableCell>
                                            <TableCell align="right">Producer</TableCell>
                                            <TableCell align="right">Quantity</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {goods.map((item) => (
                                            <TableRow
                                                key={`table-item-${item.id}`}
                                                className={clsx(item.id === rowId && classes.activeTable)}
                                                onClick={event => setRowId(item.id)}
                                            >
                                                <TableCell component="th" scope="row">
                                                    {item.name}
                                                </TableCell>
                                                <TableCell align="right">{item.description}</TableCell>
                                                <TableCell align="right">{item.price}</TableCell>
                                                <TableCell align="right">{item.producer}</TableCell>
                                                <TableCell align="right">{item.quantity}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <IconButton onClick={event => setDataDialogOpen(true)}>
                                <AddIcon/>
                            </IconButton>
                            <IconButton onClick={event => setDataDialogOpen(true)}>
                                <EditIcon/>
                            </IconButton>
                        </Box>
                    </Paper>
                </Grid>
            </Grid>
            <DataDialogEditor
                open={dataDialogOpen}
                onClose={() => setDataDialogOpen(false)}
                onFinish={() => {setDataDialogOpen(false); handleUpdate()}}
            />
        </React.Fragment>
    );
}