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
import {useConfirmDialog} from "../../Utilities/ConfirmDialog";
import DeleteIcon from '@material-ui/icons/Delete';
import {useAuth} from "../../Utilities/Auth";
import Typography from "@material-ui/core/Typography";

function DataDialogEditor({onClose, onFinish, open, idata}) {
    const defaultData = {
        name: '',
        description: '',
    };
    const [data, setData] = React.useState(idata || defaultData);
    const [errors, setErrors] = React.useState({});
    const {token, setToken} = useAuth();
    const [backendError,setBackendError] = React.useState("");
    const classes = useStyles();

    const header = idata ? `Group: ${data.name}` : 'Group: New group';

    React.useEffect(() => {
        setData(idata || defaultData);
         setBackendError(null);
    }, [idata]);

    function handleCheckFields() {
        let noError = true;
        let newerrors = errors;

        function sfe(field, error) {
            newerrors = {...newerrors, [field]: error};
        }

        function checkEmpty(fields) {
            for (const key of fields) {
                const item = data[key];
                if (!item) {
                    if (!newerrors[key]) sfe(key, `Field can not be empty`);
                    noError = false;
                } else {
                    if (newerrors[key] === `Field can not be empty`) sfe(key, null);
                }
            }
        }

        checkEmpty(['name']);
        setErrors(newerrors);
        return noError;
    }

    function handleAdd() {
        if (!handleCheckFields()) return;

        coreRequest()
            .post('groups')
            .send(data)
            .set('token', token)
            .then(response => {
                setBackendError(null);
                onFinish && onFinish();
            })
            .catch(error => {
                switch(error.status){
                    case 401:
                        setToken(null);
                        break;
                    case 409:
                        setBackendError("Group's name already exists");
                        break;
                    default:
                        setBackendError("Error");
                        break;
                }
            });

    }

    function handleEdit() {
        coreRequest()
            .put('groups')
            .send(data)
            .query({id: +idata.id})
            .set('token', token)
            .then(response => {
                setBackendError(null);
                onFinish && onFinish();

            })
            .catch(error => {
                switch(error.status){
                    case 401:
                        setToken(null);
                        break;
                    case 409:
                        setBackendError("Group's name already exists");
                        break;
                    default:
                        setBackendError("Error");
                        break;
                }
            });
    }

    function handleInput(event) {
        event.persist();
        setData(last => ({...last, [event.target.name]: event.target.value}));
    }

    return (
        <Dialog onClose={onClose} aria-labelledby="simple-dialog-title" open={open}>
            <DialogTitle id="simple-dialog-title">{header}</DialogTitle>
            <List>
                {backendError&&
                <ListItem>
                    <Typography className = {classes.error}>
                        {backendError}
                    </Typography>
                </ListItem>
                }
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Name'}
                        name={'name'}
                        value={data.name}
                        onChange={handleInput}
                        error={errors.name}
                        helperText={errors.name && errors.name}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Description'}
                        name={'description'}
                        value={data.description}
                        onChange={handleInput}
                        error={errors.description}
                        helperText={errors.description && errors.description}
                        multiline
                        rowsMax={10}
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

export default function Groups() {
    const [groups, setGroups] = React.useState([]);
    const [rowId, setRowId] = React.useState(0);
    const [dataDialogOpen, setDataDialogOpen] = React.useState(false);
    const [search, setSearch] = React.useState('');
    const [isNewRow, setIsNewRow] = React.useState(false);
    const {token, setToken} = useAuth();
    const classes = useStyles();
    const confirm = useConfirmDialog();

    function handleUpdate() {
        coreRequest()
            .get(`groups`)
            .set('token', token)
            .then(response => {
                setGroups(response.body || []);
            })
            .catch(console.error);
    }

    function handleDelete() {
        coreRequest()
            .delete(`groups`)
            .query({id: rowId})
            .set('token', token)
            .then(response => handleUpdate())
            .catch(error => {
                switch(error.message){
                    case 401:
                        setToken(null);
                        break;
                    default:
                        break;
                }
            });
    }

    React.useEffect(() => {
        handleUpdate();
    }, []);

    React.useEffect(() => {
        if (groups.length) {
            setRowId(groups[0] && groups[0].id);
        }
    }, [groups]);

    return (
        <React.Fragment>
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Box p={1}>
                            <TableContainer>
                                <Table className={classes.table} aria-label="simple table" size={"small"}>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Name</TableCell>
                                            <TableCell align="left">Description</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {groups.map((item) => (
                                            <TableRow
                                                key={`table-item-${item.id}`}
                                                className={clsx(item.id === rowId && classes.activeTable)}
                                                onClick={event => setRowId(item.id)}
                                            >
                                                <TableCell component="th" scope="row">
                                                    {item.name}
                                                </TableCell>
                                                <TableCell align="left">{item.description}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <IconButton onClick={event => {
                                setDataDialogOpen(true);
                                setIsNewRow(true);
                            }}>
                                <AddIcon/>
                            </IconButton>
                            <IconButton
                                onClick={event => setDataDialogOpen(true)}
                                disabled={!groups.length}
                            >
                                <EditIcon/>
                            </IconButton>
                            <IconButton
                                disabled={!groups.length}
                                onClick={event => confirm(handleDelete, {title: `Are you sure you want delete group?`, text: 'Notice: all goods that belong to this group will be deleted'})}>
                                <DeleteIcon/>
                            </IconButton>
                        </Box>
                    </Paper>
                </Grid>
            </Grid>
            <DataDialogEditor
                open={dataDialogOpen}
                idata={isNewRow ? undefined : groups.find(item => item.id === rowId)}
                onClose={() => {
                    setDataDialogOpen(false);
                    setIsNewRow(false);
                }}
                onFinish={() => {
                    setDataDialogOpen(false);
                    handleUpdate();
                    setIsNewRow(false);
                }}
            />
        </React.Fragment>
    );
}